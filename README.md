# Tweety 🐦

Mini-Twitter als Lernprojekt: **Spring Boot 4** (Backend) und **Angular 22** (Frontend).

Das Projekt entstand entlang eines Lernbacklogs mit 18 Stories. Ziel war nicht ein
produktionsreifes Twitter, sondern das schrittweise Erarbeiten der wichtigsten
Konzepte beider Frameworks. Lernvereinfachungen sind unter
[Bekannte Einschränkungen](#bekannte-einschränkungen) dokumentiert.

---

## Inhalt

- [Schnellstart](#schnellstart)
- [Architektur](#architektur)
- [API-Überblick](#api-überblick)
- [Demo-Szenario](#demo-szenario)
- [Lernkonzepte pro Story](#lernkonzepte-pro-story)
- [Bekannte Einschränkungen](#bekannte-einschränkungen)
- [Technische Schulden](#technische-schulden)
- [Offene Themen](#offene-themen)

---

## Schnellstart

### Voraussetzungen

| Tool        | Version                                   |
|-------------|-------------------------------------------|
| Java (JDK)  | 25 (Eclipse Temurin)                      |
| Node.js     | 24 LTS                                    |
| Angular CLI | `npm install -g @angular/cli`             |
| Maven       | nicht nötig – Maven Wrapper ist enthalten |

### Backend starten (Port 8080)

```bash
cd backend
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

### Frontend starten (Port 4200)

```bash
cd frontend
npm install                 # nur beim ersten Mal
ng serve
```

Anwendung: <http://localhost:4200>

### Nützliche Adressen

| URL                                     | Zweck                                |
|-----------------------------------------|--------------------------------------|
| <http://localhost:4200>                 | Anwendung                            |
| <http://localhost:8080/h2-console>      | Datenbank (JDBC URL siehe unten)     |
| <http://localhost:8080/actuator/health> | Health-Status                        |
| <http://localhost:8080/actuator/info>   | Anwendungsinformationen              |

**H2-Konsole:** JDBC URL `jdbc:h2:file:./data/tweety`, Benutzer `sa`, kein Passwort.

### Tests

```bash
cd backend && ./mvnw test     # JUnit, Mockito, AssertJ
cd frontend && ng test        # Vitest
```

---

## Architektur

### Gesamtbild

```
Browser
   │
   │  HTTP (relative URLs: /api/...)
   ▼
Angular Dev-Server (4200)
   │
   │  Proxy (frontend/proxy.conf.json) leitet /api an 8080
   ▼
Spring Boot (8080)
   │
   ▼
H2-Datenbank (Datei: backend/data/tweety.mv.db)
```

Im Entwicklungsbetrieb spricht der Browser ausschließlich mit Port 4200. Der
Angular-Dev-Server leitet alle `/api`-Aufrufe an das Backend weiter. Dadurch ist
für den Browser alles dieselbe Origin, und **im Backend ist keine
CORS-Konfiguration nötig**.

### Backend: Schichten

```
controller/   HTTP: Pfade, Statuscodes, Validierung auslösen. Keine Fachlogik.
service/      Fachlogik und Transaktionsgrenzen.
repository/   Datenbankzugriff (Spring Data JPA).
entity/       JPA-Entities. Kapselung über Pflichtkonstruktoren, keine Setter.
dto/          API-Vertrag. Entities verlassen die Anwendung nie.
exception/    Eigene Exceptions + zentrale Fehlerbehandlung.
event/        TweetCreatedEvent und die Listener für Folgeaktionen.
```

Jede Schicht kennt nur die darunterliegende:

- Der **Controller** kennt HTTP, aber nichts von der Datenbank.
- Der **Service** kennt die Fachregeln, aber nichts von HTTP.
- Das **Repository** kennt die Datenbank, aber nichts von der Fachlogik.

### Datenmodell

```
UserEntity ──1:n──> TweetEntity ──1:n──> LikeEntity <──n:1── UserEntity

AuditEntity (ohne Fremdschlüssel, bewusst entkoppelt)
```

**Bewusste Entscheidungen:**

- **`LikeEntity` statt `@ManyToMany`.** Eine eigene Entität macht die Beziehung
  explizit und erlaubt spätere Erweiterungen (z. B. Zeitpunkt des Likes). Ein
  zusammengesetzter Unique-Constraint auf `(tweet_id, user_id)` garantiert auf
  Datenbankebene, dass jeder Benutzer einen Tweet höchstens einmal liken kann.
- **`AuditEntity` ohne Fremdschlüssel.** Audit-Einträge speichern nur IDs. So
  überlebt der Eintrag das Löschen des protokollierten Tweets.
- **Keine `@OneToMany`-Collections.** Einseitige Beziehungen sind einfacher, und
  die Like-Anzahl kommt effizienter über eine Unterabfrage in der Projection.

### Nachbearbeitung über Events

Nach dem Erstellen eines Tweets veröffentlicht der `TweetService` ein
`TweetCreatedEvent`. Er kennt keine Empfänger.

| Listener        | Annotation                                   | Läuft                         |
|-----------------|----------------------------------------------|-------------------------------|
| `AuditListener` | `@EventListener`                             | in der Transaktion            |
| `EmailListener` | `@TransactionalEventListener(AFTER_COMMIT)`  | nach erfolgreichem Commit     |
| `SmsListener`   | `@TransactionalEventListener(AFTER_COMMIT)`  | nach erfolgreichem Commit     |

Der Audit-Eintrag ist ein fachlich notwendiger Datenbankschritt und gehört in
dieselbe Transaktion wie der Tweet. E-Mail und SMS sind technische
Nebenwirkungen: Sie laufen erst nach dem Commit und können den ursprünglichen
Aufruf nicht mehr zum Scheitern bringen.

Nachweisbar über die Fehleroption `app.simulate-failure-after-tweet=true`: Bei
einem Rollback werden weder Tweet noch Audit-Eintrag gespeichert, und E-Mail und
SMS werden gar nicht erst ausgelöst.

### Frontend: Verantwortlichkeiten

```
*-api.ts       HTTP-Aufrufe. Geben Observables zurück, halten keinen State.
*-store.ts     Zentraler State in Signals + Methoden, die ihn verändern.
Komponenten    Darstellung, Formulare, lokale Anzeigezustände.
auth.guard.ts  UI-Führung: ohne Benutzer zum Login.
```

**Signals als State-Technik:**

| Baustein     | Zweck                     | Beispiel                              |
|--------------|---------------------------|---------------------------------------|
| `signal()`   | Zustand halten            | `_tweets`, `_user`                    |
| `computed()` | Werte ableiten            | `tweetCount`, `isLoggedIn`, `remaining` |
| `effect()`   | Seiteneffekt auslösen     | Fake-Login in `localStorage` schreiben |

Zustand wird in Services gehalten und nach außen nur lesbar angeboten
(`asReadonly()`). Komponenten greifen nie direkt auf `localStorage` zu.

---

## API-Überblick

Alle Endpunkte liegen unter `/api`. Fehler kommen einheitlich als `ApiError`.

### Benutzer

| Methode | Pfad              | Zweck                      | Statuscodes        |
|---------|-------------------|----------------------------|--------------------|
| `POST`  | `/api/users`      | Benutzer anlegen           | 201, 400, 409      |
| `GET`   | `/api/users`      | alle Benutzer (Fake-Login) | 200                |
| `GET`   | `/api/users/{id}` | Profil                     | 200, 404           |

### Tweets

| Methode  | Pfad               | Zweck                      | Statuscodes        |
|----------|--------------------|----------------------------|--------------------|
| `POST`   | `/api/tweets`      | Tweet anlegen              | 201, 400, 404      |
| `GET`    | `/api/tweets`      | Timeline (paginiert)       | 200                |
| `PUT`    | `/api/tweets/{id}` | Tweet ändern               | 200, 400, 403, 404 |
| `DELETE` | `/api/tweets/{id}` | Tweet löschen              | 204, 403, 404      |

Query-Parameter bei `GET /api/tweets`: `page`, `size`, `currentUserId`
(steuert `likedByMe`).
Query-Parameter bei `DELETE`: `editorId`.

### Likes

| Methode  | Pfad                        | Zweck            | Statuscodes |
|----------|-----------------------------|------------------|-------------|
| `POST`   | `/api/tweets/{id}/likes`    | liken            | 201, 404    |
| `DELETE` | `/api/tweets/{id}/likes`    | Like entfernen   | 200, 404    |

Query-Parameter: `userId`. Beide Operationen sind idempotent: Mehrfaches
Aufrufen führt zum selben Endzustand.

### Konfiguration und Betrieb

| Methode | Pfad                | Zweck                          |
|---------|---------------------|--------------------------------|
| `GET`   | `/api/config`       | max. Tweet-Länge fürs Frontend |
| `GET`   | `/actuator/health`  | Health-Status                  |
| `GET`   | `/actuator/info`    | Anwendungsinformationen        |

### Fehlerformat

Alle Fehler nutzen dasselbe Format (`GlobalExceptionHandler`):

```json
{
  "status": 400,
  "message": "Die Eingaben sind ungültig.",
  "fieldErrors": {
    "username": "Benutzername darf nicht leer sein.",
    "email": "E-Mail ist keine gültige Adresse."
  },
  "timestamp": "2026-09-30T14:12:00Z"
}
```

| Status | Bedeutung                                              |
|--------|--------------------------------------------------------|
| 400    | Validierungsfehler oder Tweet zu lang                  |
| 403    | fachlich nicht erlaubt (fremder Tweet)                 |
| 404    | Benutzer oder Tweet existiert nicht                    |
| 409    | Konflikt (Benutzername oder E-Mail bereits vergeben)   |

---

## Demo-Szenario

Ablauf für eine Vorführung in etwa fünf Minuten.

**Vorbereitung:** Backend und Frontend starten, Backend-Log sichtbar halten,
H2-Konsole in einem zweiten Tab geöffnet.

1. **Benutzer anlegen** – „Create a new user“, Formular leer absenden. Die
   Validierungsfehler erscheinen pro Feld, ohne dass ein Request rausgeht
   (Netzwerk-Tab zeigt nichts).
2. **Gültig anlegen** – Erfolgsmeldung mit vergebener ID.
3. **Konflikt zeigen** – Denselben Benutzernamen erneut anlegen. Die Meldung
   kommt jetzt vom Server: HTTP 409 mit dem konkreten Namen.
4. **Fake-Login** – Benutzer auswählen, Weiterleitung zur Timeline.
   Seite neu laden: Die Auswahl bleibt erhalten (`localStorage`, gesetzt per
   `effect()`).
5. **Tweet erstellen** – Zeichenzähler läuft rückwärts, Button ist bei leerem
   Text deaktiviert. Nach dem Absenden erscheint der Tweet sofort oben.
6. **Event-Logs zeigen** – Im Backend-Log: Audit-Eintrag **vor** dem Commit,
   E-Mail und SMS **danach**.
7. **Liken** – Herz füllt sich, Zähler steigt. Mit einem zweiten Benutzer
   einloggen: gleiche Anzahl, aber leeres Herz (`likedByMe` pro Benutzer).
8. **Bearbeiten und Löschen** – Nur bei eigenen Tweets sichtbar. Änderungen
   erscheinen sofort, ohne Neuladen (Immutable Update im Signal).
9. **Profil öffnen** – Über den Link in der Kopfzeile. Danach
   `/users/9999` aufrufen: fachliche Not-found-Anzeige statt Fehlerseite.
10. **Guard zeigen** – Ausloggen und `/timeline` direkt aufrufen: Weiterleitung
    zum Login.
11. **Actuator** – `/actuator/health` zeigt `UP`, `/actuator/info` die
    Anwendungsinformationen.
12. **Rollback zeigen** (optional) – `app.simulate-failure-after-tweet=true`
    setzen, neu starten, Tweet anlegen. Im Log stehen beide `insert`, aber in der
    H2-Konsole ist nichts angekommen, und E-Mail/SMS blieben aus.

---

## Lernkonzepte pro Story

| Story | Thema                        | Konzepte                                                        |
|-------|------------------------------|-----------------------------------------------------------------|
| 1     | Projektgerüst                | Spring Initializr, Maven, Angular CLI, Dev-Proxy statt CORS      |
| 2     | Benutzer registrieren        | Layered Architecture, Constructor Injection, JPA, Bean Validation |
| 3     | Profil anzeigen              | Path Variables, DTO als API-Vertrag, Entity-zu-DTO-Mapping       |
| 4     | Fake-Login                   | Angular Signals, `localStorage`, programmatische Navigation       |
| 5     | Tweet erstellen              | `@ManyToOne`, Request-DTOs, Reactive Forms                       |
| 6     | Timeline                     | Query Methods mit Sortierung, `signal()`, `computed()`           |
| 7     | Bearbeiten und Löschen       | REST-Semantik (PUT/DELETE), Dirty Checking, Immutable Updates    |
| 8     | Konfigurierbare Länge        | `application.properties`, `@Value`, Konfiguration vs. Fachcode   |
| 9     | Lombok-Refactoring           | `@RequiredArgsConstructor`, Annotation Processing, Risiken `@Data` |
| 10    | Globale Fehlerbehandlung     | `@RestControllerAdvice`, HTTP-Statuscodes, einheitlicher Vertrag |
| 11    | Likes                        | Unique-Constraint über zwei Spalten, Projection mit Unterabfragen |
| 12    | Signal Store                 | `signal` / `computed` / `effect` trennen, Readonly Signals        |
| 13    | Nachbearbeitung (Interfaces) | Polymorphie, Collection Injection, Open/Closed Principle          |
| 14    | Transaktionen                | Transaktionsgrenze, Rollback, Proxies, Self-Invocation           |
| 15    | Events                       | Publish/Subscribe, `@TransactionalEventListener`, lose Kopplung  |
| 16    | Frontend abrunden            | Route Guards als UI-Führung, Zustände sichtbar machen            |
| 17    | Actuator                     | Health vs. Metrics, bewusste Endpunkt-Freigabe                   |
| 18    | Dokumentation                | Architektur kommunizieren, Schulden transparent machen           |

### Interfaces (Story 13) vs. Events (Story 15)

Beide Ansätze wurden bewusst nacheinander gebaut, um sie vergleichen zu können.

|                              | Interfaces              | Events                           |
|------------------------------|-------------------------|----------------------------------|
| Auslösung                    | Service ruft auf        | Service meldet nur               |
| Kopplung                     | Service kennt Interface | Service kennt nur das Event      |
| Auffindbarkeit im Code       | gut (Schleife sichtbar) | schlechter (Listener suchen)     |
| Zeitpunkt steuerbar          | nein                    | ja (`AFTER_COMMIT`)              |
| Fehler eines Empfängers      | reißt den Aufruf mit    | nach Commit ohne Auswirkung      |

Die Interface-Lösung wurde in Story 15 entfernt. Entscheidend war der letzte
Punkt: Ein fehlgeschlagener simulierter SMS-Versand durfte nicht verhindern,
dass ein Tweet gespeichert wird.

---

## Bekannte Einschränkungen

### Der Fake-Login ist keine Authentifizierung

Das ist die wichtigste Einschränkung des Prototyps. Konkret bedeutet das:

- **Keine Identitätsprüfung.** Jeder kann jeden Benutzer aus einer Liste
  auswählen. Es gibt kein Passwort, kein Token, kein Spring Security.
- **Das Backend vertraut dem Client.** Die Benutzer-ID kommt als `authorId`,
  `editorId` bzw. `userId` im Request und wird ungeprüft übernommen. Wer die
  API direkt aufruft, kann jede beliebige ID angeben.
- **Der `localStorage` ist manipulierbar.** In den Entwicklertools lässt sich
  der gespeicherte Benutzer beliebig ändern.
- **Der Route Guard schützt nichts.** Er läuft im Browser und ist reine
  UI-Führung. Er verhindert, dass jemand versehentlich auf einer leeren Seite
  landet, mehr nicht.
- **Die Autorprüfung ist Fachlogik, keine Zugriffssicherheit.** Dass nur der
  Autor seinen Tweet ändern darf, wird im Service geprüft – aber auf Basis
  einer ID, die der Client selbst mitschickt.

Für einen echten Betrieb müssten Authentifizierung und Autorisierung serverseitig
erfolgen, und die Benutzer-ID käme aus dem geprüften Sicherheitskontext statt aus
dem Request.

### Weitere Vereinfachungen

- **Simulierte Folgeaktionen.** E-Mail und SMS erzeugen nur Log-Ausgaben. Für die
  SMS wird der Anzeigename statt einer Telefonnummer verwendet, weil das
  Datenmodell keine Nummer kennt.
- **Fehleroption im Produktivcode.** `app.simulate-failure-after-tweet` dient nur
  der Demonstration des Rollback-Verhaltens und hätte in einer echten Anwendung
  nichts zu suchen.
- **Keine Paginierung im Frontend.** Das Backend liefert paginiert, das Frontend
  zeigt nur die erste Seite. `tweetCount` zählt daher die geladenen, nicht alle
  Tweets.
- **E-Mail im öffentlichen Profil.** Für den Prototyp bewusst in Kauf genommen.

---

## Technische Schulden

| Thema                             | Beschreibung                                                                 |
|-----------------------------------|------------------------------------------------------------------------------|
| `PageImpl` im Controller          | Spring warnt zur Laufzeit: Die JSON-Struktur ist nicht stabil. Sauber wäre ein eigenes Seiten-DTO. |
| Likes beim Bearbeiten             | `TweetResponse.from(TweetEntity)` liefert `likeCount: 0`. Das Frontend behält die alten Werte, statt dass das Backend nachlädt. |
| Spaltenlänge vs. Konfiguration    | `text` erlaubt in der Datenbank mehr Zeichen als die konfigurierte Grenze.     |
| Schema-Verwaltung                 | `ddl-auto=update` statt Flyway oder Liquibase. Für Schemaänderungen mit Daten ungeeignet. |
| Mapping                           | Von Hand in den DTOs. MapStruct würde den Code erzeugen.                       |
| `ReflectionTestUtils` in Tests    | Wird zum Setzen von IDs genutzt. In manchen Projekten nicht erlaubt; Alternativen sind Spies oder Test-Setter. |
| Frontend-Testabdeckung            | Store und Timeline sind getestet, die übrigen Komponenten nur per generiertem „should create“. |
| Statische Codeanalyse             | Kein Checkstyle, PMD oder SonarQube eingebunden.                               |

---

## Offene Themen

Priorisiert nach Nutzen für ein reales Projekt:

1. **Echte Authentifizierung** – Spring Security mit Passwörtern und
   JWT/Session. Ersetzt den Fake-Login und macht die Autorprüfung zu echter
   Zugriffssicherheit. Größter Gewinn, größter Aufwand.
2. **Datenbankmigrationen mit Flyway** – Macht Schemaänderungen nachvollziehbar
   und wiederholbar. Voraussetzung für jeden echten Betrieb.
3. **Statische Codeanalyse** – Checkstyle im Maven-Build, ESLint im Frontend.
   Fängt Stilprobleme automatisch ab, bevor ein Review sie findet.
4. **Paginierung im Frontend** – „Mehr laden“ oder Seitennavigation. Aktuell
   verpufft die Backend-Paginierung.
5. **Mehr Frontend-Tests** – Vor allem für Login, Profil und das
   Registrierungsformular.
6. **Komponentenaufteilung** – Eine `TweetCard`-Komponente mit `input()` und
   `output()` würde Darstellung und Zustand sauberer trennen.
7. **MapStruct** – Lohnt sich, sobald weitere Entitäten dazukommen.
8. **Folgen und personalisierte Timeline** – Die fachlich naheliegendste
   Erweiterung.
