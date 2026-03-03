# Incident Track Frontend

## Incident and Task Workflow Changes

### Backend changes
- Managers can now access only the incidents that belong to their own department on all admin-manager incident endpoints.
- Managers can now access only the tasks that belong to incidents in their own department on the shared task endpoints.
- Task creation is now enforced as one incident to one task at the business-logic level.
- A task can now be created only when the incident is `OPEN`.
- A second task cannot be created for the same incident.
- Managers can create tasks only for incidents in their own department.
- Managers can assign tasks only to `EMPLOYEE` users in the same department.
- Creating a task automatically moves the linked incident to `IN_PROGRESS`.
- Creating a task automatically starts the task in `PENDING`.
- A task for a closed incident cannot be created.
- A task for an incident that is already `IN_PROGRESS` cannot be created again.
- The assigned manager or assigned employee can move the task only through `PENDING -> IN_PROGRESS -> COMPLETED`.
- `ADMIN` users are no longer part of the task status workflow.
- Completing the single task automatically resolves the linked incident.
- Closed incidents (`RESOLVED` or `CANCELLED`) can no longer be updated.
- Incidents with an active task (`IN_PROGRESS`) must now be closed through task completion, not through the manual incident status endpoint.
- Incident responses now include `departmentName`.
- Incident responses now include `resolvedDate`.
- Cancelling an incident now stores a close timestamp through `resolvedDate`.
- The task status update endpoint response text was corrected from `Incident status updated` to `Task status updated`.

### Frontend changes
- Manager incident screens now reflect department-scoped access instead of global access.
- Manager task screens now reflect department-scoped task access instead of personal-assignment-only wording.
- The incidents list now labels manager scope as `Department Incidents`.
- The home page now labels manager dashboard summaries as `Department Incidents` and `Department Tasks`.
- The incidents list now shows a direct `Task` action for managers on `OPEN` incidents.
- The incident detail page now shows a direct `Create Task` action for managers on `OPEN` incidents.
- Manual incident status actions in the UI are now limited to closing an `OPEN` incident (`RESOLVED` or `CANCELLED`).
- Incident status actions are hidden once the incident is already in progress or closed.
- The task creation screen now shows only `OPEN` incidents.
- The task creation screen now explains that task creation is limited to open incidents in the manager's department.
- The task creation screen now shows a disabled state when no open incidents are available.
- The task detail screen now shows only the next valid status transition:
  `PENDING -> IN_PROGRESS`
  `IN_PROGRESS -> COMPLETED`
- The task detail screen hides the update controls once the task is already completed.
- The task list page for managers now describes the list as tasks for incidents in the manager's department.

### Additional logic covered
- A closed incident effectively freezes the task workflow as well, because task updates for closed incidents are now blocked on the backend.
- The frontend prefilled incident flow (`/tasks/create?incidentId=...`) is now connected to incident actions so managers can assign a task directly from incident views.
