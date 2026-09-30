# Tweety 🐦

Mini-Twitter als Lernprojekt: **Spring Boot 4** (Backend) und **Angular 22** (Frontend).

Das Projekt entstand entlang eines Lernprojektes. Ziel war nicht ein
produktionsreifes Twitter, sondern das schrittweise Erarbeiten der wichtigsten
Konzepte beider Frameworks.

---

## Inhalt

- [Schnellstart](#schnellstart)
- [Architektur](#architektur)
- [API-Überblick](#api-überblick)

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
