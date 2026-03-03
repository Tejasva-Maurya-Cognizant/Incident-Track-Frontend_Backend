# Backend Module Guide

Use this folder as the backend code map. Each module guide points to the main classes that own the behavior for that area of the system.

## Reading order

1. [Auth and users](./modules/auth-and-users/README.md)
2. [Incidents and tasks](./modules/incidents-and-tasks/README.md)
3. [Reference data](./modules/reference-data/README.md)
4. [Operations and reporting](./modules/operations-and-reporting/README.md)

## Cross-cutting files worth knowing early

- [BackendApplication.java](../src/main/java/com/incidenttracker/backend/BackendApplication.java)
- [SecurityService.java](../src/main/java/com/incidenttracker/backend/common/security/SecurityService.java)
- [GlobalExceptionHandler.java](../src/main/java/com/incidenttracker/backend/common/exception/GlobalExceptionHandler.java)
- [PagedResponse.java](../src/main/java/com/incidenttracker/backend/common/dto/PagedResponse.java)
- [DateTimeUtils.java](../src/main/java/com/incidenttracker/backend/common/util/DateTimeUtils.java)

## What to expect in the codebase

- Controllers are thin and mostly assemble paging + sorting arguments.
- Services contain the workflow rules that matter for the product.
- Repositories rely heavily on Spring Data derived queries, so many business rules can be understood from method names alone.
- DTOs are used for external API contracts even when entities are richer internally.
