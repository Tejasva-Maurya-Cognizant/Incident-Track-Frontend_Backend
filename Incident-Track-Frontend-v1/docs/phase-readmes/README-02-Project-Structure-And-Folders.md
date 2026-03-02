# Frontend Phase 2: Project Structure, Folders, and Responsibilities

This file explains how the frontend is organized and why the folder layout matters.

## 1. High-level source structure

Inside `src`, the main folders are:

- `assets`
- `components`
- `config`
- `context`
- `features`
- `lib`
- `pages`
- `router`
- `types`

This is a layered structure. Each folder has a specific responsibility.

## 2. Folder-by-folder explanation

### `src/assets`

Static frontend assets such as starter images or icons.

### `src/components`

Reusable UI building blocks. This folder is split into:

- `common`: generic reusable UI
- `layout`: app shell components
- `charts`: analytics/chart wrappers

Examples:

- `Pagination.tsx`
- `SortableHeader.tsx`
- `StatusBadge.tsx`
- `ProfilePanel.tsx`
- `Header.tsx`
- `Sidebar.tsx`
- `ReportCharts.tsx`

### `src/config`

Shared constants.

Current file:

- `constants.ts`

### `src/context`

Global shared state providers:

- `AuthContext.tsx`
- `NotificationContext.tsx`

Purpose:

- avoid prop drilling for app-wide values

### `src/features`

Feature-based domain/data layer. Each folder usually contains:

- `api.ts`
- `types.ts`

Feature folders:

- `auth`
- `categories`
- `compliance`
- `departments`
- `incidents`
- `notifications`
- `reporting`
- `tasks`

### `src/lib`

Low-level shared setup.

In this project:

- Axios instance
- Axios interceptors

### `src/pages`

Route-level screens.

Direct pages:

- `LoginPage.tsx`
- `RegisterPage.tsx`
- `HomePage.tsx`
- `NotFoundPage.tsx`
- `UnauthorizedPage.tsx`

Nested folders:

- `admin`
- `compliance`
- `employee`
- `incidents`
- `manager`
- `notifications`
- `tasks`

### `src/router`

Routing and access control:

- `AppRouter.tsx`
- `ProtectedRoute.tsx`
- `RoleGuard.tsx`

### `src/types`

Shared cross-feature types.

Examples:

- pagination contracts
- common utility types

## 3. Real architectural layering

A practical way to understand the frontend is:

1. `config` and `lib` define global setup.
2. `features` define backend communication.
3. `context` defines shared state.
4. `router` defines navigation and access.
5. `components` define reusable UI.
6. `pages` combine everything into screens.

That is the true layer-by-layer structure of this frontend.

## 4. How a typical feature flows

Example: incidents list.

1. `features/incidents/types.ts` defines the response model.
2. `features/incidents/api.ts` defines functions like `listMinePaged()`.
3. `pages/incidents/IncidentsListPage.tsx` calls those functions.
4. `components/common/Pagination.tsx` and badges help render the UI.
5. `router/AppRouter.tsx` exposes the page at `/incidents`.

This same pattern repeats across tasks, users, notifications, and reports.

## 5. Why this structure is good

Benefits:

- easier onboarding
- less duplicated API logic
- centralized global state
- reusable UI primitives
- safer long-term maintenance

For interviews, describe it as:

"A layered React frontend with route-level pages, reusable components, feature-based API modules, and Context-based shared state."

## 6. Important note about older files

The router currently uses `HomePage` as the main landing screen.

There are also older dashboard files:

- `pages/admin/AdminDashboard.tsx`
- `pages/manager/ManagerDashboard.tsx`
- `pages/employee/EmployeeDashboard.tsx`

In `AppRouter.tsx`, an older dashboard-based route block is commented out. So those files may be legacy or alternate implementations, not the active flow.

When modifying the project, always verify usage from the router first.
