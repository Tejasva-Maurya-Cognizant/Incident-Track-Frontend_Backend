# Operations And Reporting

This guide groups the operational support modules: notifications, compliance, audit logging, reporting, and the shared platform classes that help those modules work together.

## Core files

- [NotificationController.java](../../../src/main/java/com/incidenttracker/backend/notification/controller/NotificationController.java)
- [NotificationService.java](../../../src/main/java/com/incidenttracker/backend/notification/service/NotificationService.java)
- [NotificationRepository.java](../../../src/main/java/com/incidenttracker/backend/notification/repository/NotificationRepository.java)
- [ComplianceController.java](../../../src/main/java/com/incidenttracker/backend/audit_v1/controller/ComplianceController.java)
- [AuditService.java](../../../src/main/java/com/incidenttracker/backend/audit_v1/service/AuditService.java)
- [SlaComplianceService.java](../../../src/main/java/com/incidenttracker/backend/audit_v1/service/SlaComplianceService.java)
- [AuditLogRepository.java](../../../src/main/java/com/incidenttracker/backend/audit_v1/repository/AuditLogRepository.java)
- [IncidentSlaBreachRepository.java](../../../src/main/java/com/incidenttracker/backend/audit_v1/repository/IncidentSlaBreachRepository.java)
- [ReportController.java](../../../src/main/java/com/incidenttracker/backend/reporting_v1/controller/ReportController.java)
- [ReportServiceImpl.java](../../../src/main/java/com/incidenttracker/backend/reporting_v1/service/impl/ReportServiceImpl.java)
- [ReportRepository.java](../../../src/main/java/com/incidenttracker/backend/reporting_v1/repository/ReportRepository.java)
- [GlobalExceptionHandler.java](../../../src/main/java/com/incidenttracker/backend/common/exception/GlobalExceptionHandler.java)

## Notifications

`NotificationService.java` handles two delivery channels at once:

- database persistence for durable inbox history
- in-memory SSE emitters for live push to active browser tabs

Important details in that file:

- active emitters are stored per user id
- the service caps SSE connections per user to reduce connection storms
- events are pushed immediately after the notification row is saved
- helper methods generate domain-specific notifications for incidents, tasks, withdrawals, resolutions, and SLA breaches

This is the module that connects the backend to the frontend's live notification dropdown and notifications page.

## Compliance and audit

### Audit logging

`AuditService.java` is intentionally small. Other services call `log(...)` whenever a meaningful business event happens, which keeps the audit trail consistent across modules.

### Scheduled SLA breach detection

`SlaComplianceService.java` is the scheduled monitor:

- it runs every 5 minutes
- it finds incidents whose `slaDueAt` has passed and are not yet marked breached
- it creates an `IncidentSlaBreach` record
- it marks the incident as SLA-breached
- it sends breach notifications
- it writes an audit log entry

Because scheduling is enabled in `BackendApplication.java`, this process starts automatically with the application.

### Compliance APIs

`ComplianceController.java` exposes:

- audit logs for admins
- incident-specific audit logs
- action-type filtered audit logs
- SLA breach views for admins and managers

## Reporting

`ReportServiceImpl.java` is the analytics engine for the project. It:

- calculates incident volume trends
- calculates department performance summaries
- computes SLA compliance rates
- builds chart-friendly pie and trend datasets
- stores generated reports for later history views

The implementation is based mostly on:

- incident date-range queries from `IncidentRepository`
- completed-task lookups from `TaskRepository`
- in-memory grouping and aggregation

Reports are persisted, not just returned transiently, so the frontend can show a report history drawer.

## Shared platform support

Several `common` files make these operational modules easier to reason about:

- `PagedResponse.java`
  Standard paging wrapper reused across controllers.
- `GlobalExceptionHandler.java`
  Converts thrown exceptions into consistent API error responses.
- `DateTimeUtils.java`
  Normalizes timestamps to second precision.
- enums in `common/enums`
  Define the vocabulary for task states, incident states, notification types, action types, and more.

## Best files to read for system behavior

If you want to understand the "platform" side of the backend quickly, read these in order:

1. `NotificationService.java`
2. `SlaComplianceService.java`
3. `ComplianceController.java`
4. `ReportServiceImpl.java`
