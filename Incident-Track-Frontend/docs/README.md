# Frontend Module Guide

Use this folder as the map for the React client. The docs are grouped by runtime responsibility rather than by raw folder name, so it is easier to follow how the app actually works.

## Reading order

1. [App shell and auth](./modules/app-shell-and-auth/README.md)
2. [Incidents and tasks](./modules/incidents-and-tasks/README.md)
3. [Admin, manager, and insights screens](./modules/admin-manager-insights/README.md)
4. [Shared UI and data layer](./modules/shared-ui-and-data-layer/README.md)

## High-level architecture

- `main.tsx` boots providers and shared Axios behavior.
- `AuthContext.tsx` determines whether protected routes can render.
- `AppRouter.tsx` groups pages by access level.
- `features/*/api.ts` files are the boundary between UI pages and backend endpoints.
- `pages/*` own route-level state and orchestrate data loading.
- `components/*` keep repeated UI behavior reusable.
