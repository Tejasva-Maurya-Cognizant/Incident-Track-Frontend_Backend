# Incident Track

Incident Track is a full-stack incident management system for handling issue reporting, task assignment, SLA tracking, notifications, and operational reporting across three roles: `ADMIN`, `MANAGER`, and `EMPLOYEE`.

The repository contains:

- a Spring Boot backend API with JWT authentication, MySQL persistence, scheduled SLA monitoring, audit logging, and server-sent event (SSE) notifications
- a React + TypeScript frontend that provides role-aware dashboards, CRUD workflows, reporting screens, and live notification updates

## Main workflows

- Employees report incidents, track their own incidents, and work on assigned tasks.
- Managers see department-scoped incidents, create tasks for employees in the same department, and monitor SLA breaches plus analytics.
- Admins manage users, departments, categories, audit logs, and generated reports across the full system.

## Repository layout

```text
.
|- Incident-Track-Backend/   Spring Boot API
|- Incident-Track-Frontend/  React + Vite client
`- Docs/                     Existing study notes and frontend implementation notes
```

## Tech stack

- Backend: Java 21, Spring Boot 4, Spring Security, Spring Data JPA, MySQL, Swagger/OpenAPI
- Frontend: React 19, TypeScript, Vite, Tailwind CSS 4, Axios, React Router, Recharts

## Local setup

### Prerequisites

- Java 21
- Node.js 20+ and npm
- MySQL 8+

### 1. Create the database

Create a MySQL database named `incidenttrack_db_v2`.

The backend currently points to:

- host: `localhost`
- port: `3306`
- database: `incidenttrack_db_v2`
- username: `root`
- password: `root`

These defaults come from `Incident-Track-Backend/src/main/resources/application.properties`.

### 2. Run the backend

From `Incident-Track-Backend/`:

```powershell
.\mvnw.cmd spring-boot:run
```

Useful backend URLs after startup:

- API base: `http://localhost:8888/api`
- Swagger UI: `http://localhost:8888/swagger-ui/index.html`

Important backend notes:

- The API runs on port `8888`.
- CORS is currently configured for `http://localhost:5173` and `http://localhost:5174`.
- SQL seed files exist under `src/main/resources`, but `spring.sql.init.mode=NEVER` means they do not auto-run by default.

### 3. Run the frontend

From `Incident-Track-Frontend/`:

```powershell
npm install
npm run dev
```

Frontend dev URL:

- `http://localhost:5173`

The frontend API base URL is hardcoded in `Incident-Track-Frontend/src/config/constants.ts` as `http://localhost:8888/api`.

## Build and test

Backend:

```powershell
.\mvnw.cmd -q test
.\mvnw.cmd -q -DskipTests compile
```

Frontend:

```powershell
npm run lint
npm run build
```

## Documentation map

- [Backend overview](./Incident-Track-Backend/README.md)
- [Backend module guides](./Incident-Track-Backend/docs/README.md)
- [Frontend overview](./Incident-Track-Frontend/README.md)
- [Frontend module guides](./Incident-Track-Frontend/docs/README.md)

Existing study notes that are still useful:

- [Frontend phase notes](./Docs/Frontend/phase-readmes/README-00-Study-Guide-Index.md)
- [Frontend JS/React notes](./Docs/Frontend/js-react-notes/README-00-Index.md)

## Suggested GitHub repo description

`Role-based incident tracking platform built with Spring Boot, React, MySQL, JWT auth, live notifications, SLA monitoring, and reporting dashboards.`
