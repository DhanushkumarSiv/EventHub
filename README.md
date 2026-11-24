# EventHub

Campus Event Registration and Management System.

This repository is a local full-stack application (React + Spring Boot + PostgreSQL). Docker, CI/CD, and cloud deployment are intentionally out of scope for the current development phase.

## Local URLs

| Service | URL |
| --- | --- |
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8080 |
| API base | http://localhost:8080/api/v1 |
| PostgreSQL | localhost:5432, database `eventhub` |

## Prerequisites

- JDK 21 (`JAVA_HOME` should point at JDK 21)
- Maven Wrapper (`backend/mvnw` / `backend/mvnw.cmd`) — a system Maven install is optional
- Node.js 20+ and npm
- PostgreSQL 16+ listening on port 5432

On this Windows machine, JDK 21 is installed at:

`C:\Program Files\Java\jdk-21.0.13.0.3`

If `java -version` shows a newer JDK, set `JAVA_HOME` before running the backend.

## Milestone 1 setup

1. Copy environment placeholders:

```powershell
copy .env.example .env
```

2. Set `DB_PASSWORD` in `.env` to your local PostgreSQL `postgres` user password.

3. Create the database:

```powershell
$env:Path = "C:\Program Files\PostgreSQL\18\bin;$env:Path"
$env:PGPASSWORD = "<your-postgres-password>"
psql -h localhost -U postgres -c "SELECT 1 FROM pg_database WHERE datname = 'eventhub';"
psql -h localhost -U postgres -c "CREATE DATABASE eventhub;"
```

If the database already exists, the second command can be ignored.

4. Start the backend (PowerShell):

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.13.0.3"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
. .\scripts\load-env.ps1
cd backend
.\mvnw.cmd spring-boot:run
```

5. In a second terminal, start the frontend:

```powershell
cd frontend
copy ..\.env .env
npm install
npm run dev
```

6. Verify:

- Browser: http://localhost:5173 shows backend health
- API: http://localhost:8080/api/v1/health returns `"status": "UP"` and `"database": "eventhub"`

## Tests

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.13.0.3"
cd backend
.\mvnw.cmd test
```

`HealthControllerTest` does not need PostgreSQL. The full Spring context test runs only when `DB_PASSWORD` is set.

## Current milestone

Milestone 1: project initialization, PostgreSQL connectivity, health endpoint, and frontend scaffold.

Authentication, domain entities, and the rest of the product are implemented in later milestones.
