# Incident Track Frontend

This module is the React client for Incident Track. It provides the role-aware UI for authentication, incident reporting, task assignment and execution, notifications, admin maintenance screens, compliance views, and analytics dashboards.

## Stack

- React 19
- TypeScript
- Vite
- Tailwind CSS 4
- Axios
- React Router
- Recharts

## Start the frontend

```powershell
npm install
npm run dev
```

Default dev URL:

- `http://localhost:5173`

The API base URL is currently defined in [src/config/constants.ts](./src/config/constants.ts) as `http://localhost:8888/api`.

## Build and quality checks

```powershell
npm run lint
npm run build
```

There are no dedicated frontend test files in `src/` right now, so linting and production build checks are the main validation steps.

## Frontend structure

### App bootstrap

- [src/main.tsx](./src/main.tsx)
  Attaches Axios interceptors, clears the token on `401`, and wraps the app with auth + notification providers.
- [src/App.tsx](./src/App.tsx)
  Thin wrapper that renders the router.
- [src/router/AppRouter.tsx](./src/router/AppRouter.tsx)
  Defines the public and protected route tree.

### Route model

- Public routes
  `/login`, `/register`
- Protected shared routes
  `/`, `/incidents`, `/tasks`, `/notifications`
- Admin-only routes
  `/admin/departments`, `/admin/categories`, `/admin/users`, `/admin/audit-logs`, `/admin/reports`
- Manager-only routes
  `/manager/users`, `/manager/charts`, `/tasks/create`
- Admin + manager routes
  `/compliance/breaches`

`HomePage.tsx` is the active post-login dashboard. The legacy role-specific dashboard files still exist in `src/pages/...`, but they are not the main routed landing pages in the current router.

## Current workflow rules reflected in the UI

- Managers see department-scoped incidents and department-scoped task data.
- One incident can have only one task.
- Managers can create tasks only for `OPEN` incidents.
- The task detail screen only allows the next valid transition:
  `PENDING -> IN_PROGRESS -> COMPLETED`
- Completing the task resolves the linked incident, so the UI stops offering manual incident progression once task workflow owns it.

## Key folders

- `src/context`
  Auth state and live notification state.
- `src/features`
  API wrappers and TypeScript contracts for each backend domain.
- `src/components`
  Layout, shared UI primitives, and chart widgets.
- `src/pages`
  Route-level pages.
- `src/router`
  Route guards and router composition.
- `src/lib/axios`
  Shared Axios instance plus interceptors.

## Module guides

- [Frontend docs index](./docs/README.md)
- [App shell and auth](./docs/modules/app-shell-and-auth/README.md)
- [Incidents and tasks](./docs/modules/incidents-and-tasks/README.md)
- [Admin, manager, and insights screens](./docs/modules/admin-manager-insights/README.md)
- [Shared UI and data layer](./docs/modules/shared-ui-and-data-layer/README.md)

## Related notes

Existing deeper study notes are still available in the root `Docs/Frontend` folder:

- [Phase notes index](../Docs/Frontend/phase-readmes/README-00-Study-Guide-Index.md)
- [JS and React notes index](../Docs/Frontend/js-react-notes/README-00-Index.md)
- [Urgent workflow change summary](../Docs/Frontend/TASK_INCIDENT_URGENT_CHANGE_README.md)
