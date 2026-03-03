# Incident Track Backend

This module is the Spring Boot API for the Incident Track system. It owns authentication, authorization, persistence, business rules, scheduled SLA breach detection, audit logging, report generation, and live notification delivery.

## Stack

- Java 21
- Spring Boot 4.0.1
- Spring Security with JWT
- Spring Data JPA
- MySQL
- springdoc OpenAPI / Swagger UI

## Runtime defaults

- Port: `8888`
- Base API path: `http://localhost:8888/api`
- Database URL: `jdbc:mysql://localhost:3306/incidenttrack_db_v2`
- Allowed frontend origins: `http://localhost:5173`, `http://localhost:5174`

The main runtime config lives in [src/main/resources/application.properties](./src/main/resources/application.properties).

## Start the backend

```powershell
.\mvnw.cmd spring-boot:run
```

Alternative commands:

```powershell
.\mvnw.cmd -q -DskipTests compile
.\mvnw.cmd -q test
```

Swagger UI is available at:

- `http://localhost:8888/swagger-ui/index.html`

## Seed data

SQL seed files are already included under [src/main/resources](./src/main/resources), including departments, categories, users, incidents, tasks, notifications, reports, and SLA breach samples.

At the moment:

- `spring.sql.init.data-locations=classpath:*.sql`
- `spring.sql.init.mode=NEVER`

That means the scripts are present but not automatically applied on startup. If you want them loaded into a fresh database, switch the mode to `ALWAYS` temporarily or import the SQL files manually.

## Backend structure

### Application entry

- [BackendApplication.java](./src/main/java/com/incidenttracker/backend/BackendApplication.java)
  Starts Spring Boot and enables scheduling for the SLA monitor.

### Business packages

- `user`
  Authentication, JWT, role checks, user profile APIs, admin user management.
- `incident`
  Incident creation, filtering, viewing, withdrawal, and privileged status updates.
- `task`
  Manager task assignment plus employee/manager task status workflow.
- `department`
  Department master data.
- `category`
  Category and sub-category master data plus SLA settings.
- `notification`
  Persistent notifications and SSE push delivery.
- `audit_v1`
  Audit log storage and scheduled SLA breach detection.
- `reporting_v1`
  Historical reports, trends, summary metrics, and chart datasets.
- `common`
  Shared enums, exceptions, pagination DTOs, date utilities, and security helpers.

## Request flow

1. `SecurityConfig` installs the JWT filter and enforces stateless auth.
2. Controllers expose role-protected REST endpoints under `/api/...`.
3. Services enforce workflow and access rules.
4. Repositories handle persistence and derived JPA queries.
5. Side effects such as audit logs, notifications, and report storage are triggered inside the service layer.

## Endpoint groups

- `/api/auth`
  Login, register, profile, user listing, self-update, admin user updates.
- `/api/incidents`
  Employee incident APIs plus admin/manager privileged incident APIs.
- `/api/tasks`
  Task creation, filtering, detail, and controlled status transitions.
- `/api/departments`
  Department lookup and creation.
- `/api/categories`
  Category CRUD-like operations, visibility toggles, and lookup endpoints.
- `/api/notifications`
  SSE subscribe, inbox, unread inbox, and mark-as-read actions.
- `/api/admin/compliance`
  Audit logs and SLA breach views.
- `/api/reports`
  Report generation and chart endpoints.

## Tests

Unit and slice tests exist under [src/test/java](./src/test/java), including repositories, services, controllers, and the global exception handler.

## Module guides

- [Backend docs index](./docs/README.md)
- [Auth and users](./docs/modules/auth-and-users/README.md)
- [Incidents and tasks](./docs/modules/incidents-and-tasks/README.md)
- [Reference data](./docs/modules/reference-data/README.md)
- [Operations and reporting](./docs/modules/operations-and-reporting/README.md)
