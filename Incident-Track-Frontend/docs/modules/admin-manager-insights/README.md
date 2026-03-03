# Admin, Manager, And Insights Screens

This module groups the route-level pages used for administration, team management, compliance, and analytics.

## Core files

- [AdminUsersPage.tsx](../../../src/pages/admin/AdminUsersPage.tsx)
- [AdminDepartmentsPage.tsx](../../../src/pages/admin/AdminDepartmentsPage.tsx)
- [AdminCategoriesPage.tsx](../../../src/pages/admin/AdminCategoriesPage.tsx)
- [AdminAuditLogPage.tsx](../../../src/pages/admin/AdminAuditLogPage.tsx)
- [AdminReportsPage.tsx](../../../src/pages/admin/AdminReportsPage.tsx)
- [ManagerUsersPage.tsx](../../../src/pages/manager/ManagerUsersPage.tsx)
- [ManagerChartsPage.tsx](../../../src/pages/manager/ManagerChartsPage.tsx)
- [IncidentBreachesPage.tsx](../../../src/pages/compliance/IncidentBreachesPage.tsx)
- [features/auth/api.ts](../../../src/features/auth/api.ts)
- [features/departments/api.ts](../../../src/features/departments/api.ts)
- [features/categories/api.ts](../../../src/features/categories/api.ts)
- [features/compliance/api.ts](../../../src/features/compliance/api.ts)
- [features/reporting/api.ts](../../../src/features/reporting/api.ts)
- [ReportCharts.tsx](../../../src/components/charts/ReportCharts.tsx)

## Admin management pages

### `AdminUsersPage.tsx`

This is the main user administration screen. It typically owns:

- paged user loading
- filters and search
- create/edit flows
- status toggle actions
- department-aware form choices

It is the page where the frontend's role and status management rules meet the backend's `/api/auth` admin endpoints.

### `AdminDepartmentsPage.tsx`

This page wraps the department master-data workflow:

- list departments
- add a new department
- refresh the table after changes

The backing API is intentionally small, so this page is one of the cleaner admin flows to modify.

### `AdminCategoriesPage.tsx`

This page manages:

- category creation
- category updates
- visibility toggles
- parent/sub-category display
- SLA configuration per category

Because categories influence backend SLA and severity behavior, changes here affect the actual incident workflow, not just labels.

## Compliance and reporting

### `AdminAuditLogPage.tsx`

Uses `features/compliance/api.ts` to page through audit logs, search by incident, and filter by action type.

### `IncidentBreachesPage.tsx`

This is shared by admins and managers. It focuses on SLA breach records, which come from the backend scheduler.

### `AdminReportsPage.tsx`

This is the most complex admin screen in the frontend. It combines:

- date-range controls
- multiple report tabs
- trend and summary loads
- chart widgets
- report history
- a report detail modal/drawer

If you need to understand how larger multi-panel screens are assembled in this codebase, this is one of the best files to study.

## Manager pages

### `ManagerUsersPage.tsx`

Focused on the manager's department team rather than all users. It usually mirrors a subset of the admin users flow with tighter scope.

### `ManagerChartsPage.tsx`

This page is the manager-facing analytics dashboard. It:

- loads multiple chart datasets in parallel
- summarizes total incidents, breaches, and SLA compliance
- visualizes incidents by department, status, severity, and category
- derives SLA compliance bars from chart results

It is a good example of "dashboard composition" rather than CRUD.

## Chart components

`ReportCharts.tsx` contains the reusable chart wrappers that make `AdminReportsPage.tsx` and `ManagerChartsPage.tsx` consistent:

- card containers
- pie chart widgets
- trend charts
- bar charts
- stat pills
- SLA progress bars

When analytics visuals need to change, this file is usually the best first stop.
