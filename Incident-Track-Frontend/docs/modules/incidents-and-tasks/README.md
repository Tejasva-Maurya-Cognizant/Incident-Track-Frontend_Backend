# Incidents And Tasks

This is the main product workflow in the frontend. It covers incident reporting, filtering, viewing, task creation, and task execution.

## Core files

- [HomePage.tsx](../../../src/pages/HomePage.tsx)
- [features/incidents/api.ts](../../../src/features/incidents/api.ts)
- [features/incidents/types.ts](../../../src/features/incidents/types.ts)
- [IncidentsListPage.tsx](../../../src/pages/incidents/IncidentsListPage.tsx)
- [IncidentCreatePage.tsx](../../../src/pages/incidents/IncidentCreatePage.tsx)
- [IncidentDetailPage.tsx](../../../src/pages/incidents/IncidentDetailPage.tsx)
- [features/tasks/api.ts](../../../src/features/tasks/api.ts)
- [features/tasks/types.ts](../../../src/features/tasks/types.ts)
- [TasksListPage.tsx](../../../src/pages/tasks/TasksListPage.tsx)
- [TaskCreatePage.tsx](../../../src/pages/tasks/TaskCreatePage.tsx)
- [TaskDetailPage.tsx](../../../src/pages/tasks/TaskDetailPage.tsx)

## Home dashboard

`HomePage.tsx` is the active dashboard after login. It:

- loads recent incidents
- loads recent tasks
- changes counts and labels by role
- exposes quick actions based on what the user is allowed to do

For example, managers see "Department Incidents" and "Department Tasks", while employees see only their own scope.

## Incident screens

### `IncidentsListPage.tsx`

This page combines:

- paged loading
- server-side sort parameters
- status filters
- client-side severity filtering
- urgent-only filter
- scope switching (`MINE` vs `ALL`) for admin/manager
- direct search by incident id

The page chooses different backend endpoints depending on role and scope, which makes it a good file for understanding how frontend permissions mirror backend permissions.

### `IncidentCreatePage.tsx`

This is a dependent form:

- it loads visible categories
- it lets the user choose a parent category first
- it narrows the sub-category list from that choice
- it shows derived SLA information before submit
- it submits `urgent` with the incident payload

It is a good example of derived UI state with `useMemo`.

### `IncidentDetailPage.tsx`

This page changes behavior by role:

- employees can withdraw their own still-open incidents
- managers can jump directly into task creation for open incidents
- admins and managers can manually close only open incidents
- employees coming from a task use the task-aware incident endpoint

The `fromTask=true` query parameter is the bridge between the task module and the incident detail page.

## Task screens

### `TaskCreatePage.tsx`

This page is manager-only and encodes the current backend workflow rules:

- it loads department employees
- it loads candidate incidents
- it loads existing tasks
- it filters incidents so only open incidents without an existing task can be selected

It also supports a prefilled incident flow from `IncidentDetailPage.tsx` using `?incidentId=...`.

### `TasksListPage.tsx`

This page adapts by role:

- employees see only tasks assigned to them
- managers see department tasks
- admins see all tasks

For non-employee views, it enriches rows with user names by resolving assignee ids through `authApi.getUserById(...)`.

### `TaskDetailPage.tsx`

This page exposes only the next valid transition:

- `PENDING -> IN_PROGRESS`
- `IN_PROGRESS -> COMPLETED`

That is a deliberate UI guardrail. The page does not allow arbitrary status changes even though the backend still validates everything again.

## Why the feature API files matter

- `features/incidents/api.ts`
  Centralizes all incident endpoint shapes, including employee, admin/manager, urgent, and task-access endpoints.
- `features/tasks/api.ts`
  Makes the role split explicit by grouping manager-only, admin/manager, employee, and shared task endpoints.

If you need to change endpoint paths or request payloads, start in the relevant `api.ts` file before editing the page.
