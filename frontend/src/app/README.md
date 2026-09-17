# Tweety 🐦

Mini-Twitter-Lernprojekt mit **Spring Boot** (Backend) und **Angular** (Frontend).

## Voraussetzungen

| Tool        | Version                                      |
|-------------|----------------------------------------------|
| Java (JDK)  | 25                                           |
| Node.js     | 24 LTS                                       |
| Angular CLI | `npm install -g @angular/cli`                |
| Maven       | nicht nötig – Maven Wrapper ist enthalten    |

## Projektstruktur

```
tweety/
├── backend/    Spring Boot (REST, JPA, H2, Validation, Actuator)
└── frontend/   Angular (Standalone Components, Routing, Signals)
```

## Starten

**Backend** – läuft auf http://localhost:8080

```bash
cd backend
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
```

**Frontend** – läuft auf http://localhost:4200

```bash
cd frontend
npm install                 # nur beim ersten Mal
ng serve
```

## Testen

| URL                                   | Erwartung              |
|---------------------------------------|------------------------|
| http://localhost:8080/api/hello       | `Hello Mini-Twitter`   |
| http://localhost:8080/actuator/health | `{"status":"UP"}`      |
| http://localhost:4200/hello           | Antwort vom Backend    |

## Hinweis: Proxy statt CORS

Das Frontend ruft das Backend über relative URLs (`/api/...`) auf.
Der Angular-Dev-Server leitet diese per `frontend/proxy.conf.json` an Port 8080 weiter –
im Backend ist daher keine CORS-Konfiguration nötig.