# Incidents And Tasks

This is the main business workflow for the product. Incidents are reported first, then managers can turn eligible incidents into tasks. Task completion closes the linked incident.

## Core files

- [IncidentController.java](../../../src/main/java/com/incidenttracker/backend/incident/controller/IncidentController.java)
- [IncidentServiceImpl.java](../../../src/main/java/com/incidenttracker/backend/incident/service/impl/IncidentServiceImpl.java)
- [IncidentRepository.java](../../../src/main/java/com/incidenttracker/backend/incident/repository/IncidentRepository.java)
- [Incident.java](../../../src/main/java/com/incidenttracker/backend/incident/entity/Incident.java)
- [TaskController.java](../../../src/main/java/com/incidenttracker/backend/task/controller/TaskController.java)
- [TaskServiceImpl.java](../../../src/main/java/com/incidenttracker/backend/task/service/impl/TaskServiceImpl.java)
- [TaskRepository.java](../../../src/main/java/com/incidenttracker/backend/task/repository/TaskRepository.java)
- [Task.java](../../../src/main/java/com/incidenttracker/backend/task/entity/Task.java)

## Incident workflow

### Incident creation

`IncidentServiceImpl.java` does more than save a row:

- loads the selected category
- derives the current authenticated user as the reporter
- calculates `slaDueAt` from the category's SLA hours
- halves the effective SLA window when the incident is marked urgent
- derives `calculatedSeverity`
- saves the incident
- writes an audit entry
- notifies managers in the incident's department

### Incident visibility

- Employees can see their own incidents.
- Employees can also view an incident from a task context if a task for that incident is assigned to them.
- Managers can access incidents only for their own department on the privileged endpoints.
- Admins can access all incidents.

The department scoping is implemented directly in service methods such as `getPrivilegedAccessibleIncident(...)`.

### Incident status rules

Manual status updates are intentionally restricted:

- closed incidents (`RESOLVED`, `CANCELLED`) cannot be changed
- `IN_PROGRESS` incidents cannot be manually closed through the incident endpoint if there is an active task workflow
- manual updates only allow `RESOLVED` or `CANCELLED`

Employees can withdraw only their own still-open incidents through `withdrawIncident(...)`.

## Task workflow

### Why `TaskRepository.java` matters

[TaskRepository.java](../../../src/main/java/com/incidenttracker/backend/task/repository/TaskRepository.java) is the quickest way to understand task visibility and scoping. Its query methods show the supported access patterns:

- by task id
- by incident id
- by assignee
- by assigner
- by status
- by department through nested `incident -> category -> department`

It also exposes the business guard `existsByIncident_IncidentId(...)`, which is part of the one-task-per-incident rule.

### Task creation rules

`TaskServiceImpl.createTask(...)` enforces these constraints:

- only managers can create tasks
- the manager can only create tasks for incidents in the manager's own department
- the linked incident must be `OPEN`
- a closed incident cannot receive a task
- an incident can have only one task
- the assignee must be an `EMPLOYEE`
- the assignee must belong to the same department as the manager

Once the task is created:

- the incident is moved to `IN_PROGRESS`
- the task starts in `PENDING`
- an audit entry is written
- the assigned employee is notified

### Task status progression

The task workflow is intentionally linear:

- `PENDING -> IN_PROGRESS`
- `IN_PROGRESS -> COMPLETED`

Only the assigned employee or the assigning manager can advance the task. When the task reaches `COMPLETED`, the linked incident is automatically moved to `RESOLVED` and its `resolvedDate` is set.

## Data model links

- [Incident.java](../../../src/main/java/com/incidenttracker/backend/incident/entity/Incident.java)
  Stores reporter, category, urgency, severity, timestamps, and SLA flags.
- [Task.java](../../../src/main/java/com/incidenttracker/backend/task/entity/Task.java)
  Uses a `@OneToOne` relationship to `Incident`, which makes the one-task-per-incident rule part of the persistence model as well as the service logic.

## Practical reading order

1. `IncidentController.java`
2. `IncidentServiceImpl.java`
3. `TaskController.java`
4. `TaskServiceImpl.java`
5. `TaskRepository.java`

That order makes the workflow easy to follow from API entry point to persistence rule.
