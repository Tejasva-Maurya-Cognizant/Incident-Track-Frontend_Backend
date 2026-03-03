# Frontend Phase 5: Actual Feature Implementation, Module by Module

This file explains how the business features are built and how the layers connect in practice. Read this as implementation notes, not just a folder summary.

## 1. Practical build order if recreating from scratch

If I had to rebuild this frontend, the sensible order would be:

1. setup Vite + React + TypeScript + Tailwind
2. add Axios instance and interceptors
3. add auth types and auth API
4. build `AuthContext`
5. build router with `ProtectedRoute` and `RoleGuard`
6. build `AppLayout`, `Sidebar`, `Header`
7. build shared table/form/status primitives
8. build login and register
9. build `HomePage`
10. build incidents
11. build tasks
12. add notifications
13. add admin/manager modules
14. add reporting and analytics

That order is important because later modules depend on earlier infrastructure.

## 2. Home module

`HomePage.tsx` is the post-login landing screen.

It:

- reads the logged-in user from auth context
- changes content by role
- loads incidents and tasks
- shows quick actions allowed for that role

Key role derivation:

```tsx
const role = user?.role ?? "EMPLOYEE";
const isManager = role === "MANAGER";
const isAdminOrManager = role === "ADMIN" || role === "MANAGER";
```

This is a clear example of conditional UI based on shared state.

### Important implementation note

`HomePage` is not just decorative. It acts like a real summary dashboard:

- it fetches recent incidents
- it fetches recent tasks
- it calculates counts from those arrays
- it exposes quick links into the main workflows

So even though there are separate legacy dashboard files, `HomePage` is effectively the active dashboard in the current routing structure.

## 3. Incidents module

Main files:

- `features/incidents/types.ts`
- `features/incidents/api.ts`
- `pages/incidents/IncidentsListPage.tsx`
- `pages/incidents/IncidentCreatePage.tsx`
- `pages/incidents/IncidentDetailPage.tsx`

### `IncidentsListPage`

This page supports:

- paging
- sorting
- status filter
- severity filter
- urgent-only filter
- role-based scope (`MINE` vs `ALL`)
- search by incident ID

Hooks used:

- `useState`
- `useCallback`
- `useEffect`
- `useMemo`

Reactive fetch pattern:

```tsx
useEffect(() => {
  load(params);
}, [params, scope, status, onlyUrgent]);
```

This is a core pattern you will see repeatedly in the app.

### Implementation note on filtering

This page mixes:

- backend filtering
- frontend filtering

Examples:

- status filtering often changes which API endpoint is called
- severity filtering is currently derived locally from fetched items using `useMemo`

That distinction matters when modifying the page:

- backend filtering reduces network payload and is stronger for large datasets
- frontend filtering is simpler but only works on what has already been fetched

### `IncidentCreatePage`

This page is a dependent form:

- load categories
- select a parent category
- filter sub-categories
- enter description
- optionally mark urgent
- submit

It uses `useMemo` for derived values:

```tsx
const parentNames = useMemo(() => { ... }, [categories]);
const subCategories = useMemo(() => { ... }, [categories, parentCategory]);
```

Why that matters:

- reduces repeated calculation
- avoids storing duplicated state
- makes derived form logic easier to read

### Important workflow note

This page is a good example of a dependency-based form:

- category choice controls available sub-categories
- sub-category determines the selected category record
- selected record changes what SLA information is shown
- urgent checkbox changes the displayed effective SLA window

So the page is not just "submit a form". It is a reactive form where earlier choices affect later UI.

### `IncidentDetailPage`

This page handles:

- loading one incident
- choosing API path based on role and query params
- employee withdrawal
- manager/admin close actions
- optional note on status update

This is a strong example of role-driven page behavior.

### Important routing note

This page supports multiple access paths:

- direct incident route
- incident route reached from a task

The query param `fromTask=true` changes which API path may be used for employees. That is an important cross-feature integration detail.

## 4. Tasks module

Main files:

- `features/tasks/types.ts`
- `features/tasks/api.ts`
- `pages/tasks/TasksListPage.tsx`
- `pages/tasks/TaskCreatePage.tsx`
- `pages/tasks/TaskDetailPage.tsx`

### `TasksListPage`

Behavior changes by role:

- employee: only assigned tasks
- manager: department-scoped tasks
- admin: all tasks

It also enriches task rows by fetching user details for assignee IDs.

That shows a common pattern:

- fetch main records first
- resolve related display data second

This is common when backend responses do not fully denormalize all related data needed for display.

### `TaskCreatePage`

This page loads:

- department employees
- candidate incidents
- existing tasks

Then it filters incidents so only valid ones remain.

Important async pattern:

```tsx
const [empList, incList, taskPage] = await Promise.allSettled([
  authApi.getEmployeesByDepartment(),
  incidentsApi.listAllAdminManager(),
  tasksApi.listAllPaged({ page: 0, size: 1000, sortBy: "createdDate", sortDir: "desc" }),
]);
```

Why `Promise.allSettled` is good here:

- one failure does not kill the entire batch
- partial data can still be used
- the page remains more resilient to partial backend issues

### Validation note

This page has two layers of validation:

- field presence/type checks in the frontend
- business-rule enforcement in the backend

That is the right approach. Frontend validation improves UX, but backend validation remains the real source of rule enforcement.

### `TaskDetailPage`

This page:

- loads the selected task
- checks access rules
- derives the next legal status
- only allows valid updates

Workflow:

- `PENDING -> IN_PROGRESS`
- `IN_PROGRESS -> COMPLETED`

Completed tasks show no further update controls.

### Important workflow note

This screen encodes business workflow directly in UI:

- it does not let the user choose any arbitrary status
- it only offers the next allowed transition

That means the UI is intentionally guiding the process, not just exposing raw backend enums.

## 5. Notifications module

Files:

- `features/notifications/api.ts`
- `features/notifications/types.ts`
- `context/NotificationContext.tsx`
- `pages/notifications/NotificationsPage.tsx`

Architecture:

- global context owns notification state
- header dropdown shows recent items
- page shows a full filtered list

`NotificationsPage` adds local filter state:

- all
- unread
- read

This is a good example of combining global data with local view state.

### Personal note

This module is one of the best files to study if you want to understand how:

- Context
- live subscriptions
- derived counts
- shared UI integration

fit together in a real app.

## 6. Admin and manager modules

Important pages:

- `AdminUsersPage`
- `ManagerUsersPage`
- `AdminDepartmentsPage`
- `AdminCategoriesPage`
- `AdminAuditLogPage`
- `IncidentBreachesPage`
- `AdminReportsPage`
- `ManagerChartsPage`

Shared patterns across these pages:

- paged tables
- filters
- modal-based CRUD
- `load()` functions wrapped in `useCallback`
- reusable table and pagination components

### `AdminUsersPage`

This page demonstrates CRUD flow:

- fetch users and departments
- filter by role, status, and search
- open create modal
- open edit modal
- toggle user status

It is also a strong props example because modal children receive callbacks like:

- `onClose`
- `onSaved`
- `onCreated`

This shows a clean parent-child workflow:

- parent owns list data and modal visibility
- child handles the modal form
- child reports success upward through callbacks
- parent decides when to refresh data

### `AdminReportsPage`

This is one of the most complex screens.

It combines:

- multiple tabs
- date ranges
- chart widgets
- report generation
- report history
- report detail modal

This page is useful for understanding how large multi-panel React screens are organized.

### Why this page matters for learning

It demonstrates:

- multiple independent pieces of state in one page
- tab-driven loading behavior
- multiple async loaders
- composition with chart components
- modal detail views for complex data

If you can read and explain this page clearly, your React confidence will improve a lot.

## 7. Repeated integration pattern across the project

Most pages follow the same build recipe:

1. import API functions and types
2. create local state with `useState`
3. define `load()` or submit handlers
4. use `useEffect` for initial or reactive fetches
5. render loading, error, empty, or success UI
6. compose reusable shared components

If you understand that pattern, you can confidently modify most of this frontend.

## 8. Personal shorthand notes for modification

When changing any feature in this project, the shortest reliable method is:

1. find the route
2. open the page component
3. identify which context values it depends on
4. identify which feature API methods it calls
5. identify which shared components it renders
6. change one layer at a time

That avoids the most common mistake beginners make: editing UI without understanding where the data really comes from.
