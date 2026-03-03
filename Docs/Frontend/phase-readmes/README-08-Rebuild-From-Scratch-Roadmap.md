# Frontend Phase 8: Rebuild From Scratch Roadmap

This file is a practical reconstruction guide. The goal is to answer:

"If I had to recreate this frontend from scratch without copying files, what order would I follow?"

## 1. Stage 1: Create the base app

Start with:

- Vite React TypeScript app
- Tailwind setup
- ESLint setup

Minimum files to understand first:

- `index.html`
- `src/main.tsx`
- `src/App.tsx`
- `src/index.css`

Goal of this stage:

- render a blank app
- confirm the app boots

## 2. Stage 2: Add configuration and HTTP layer

Create:

- `src/config/constants.ts`
- `src/lib/axios/axiosInstance.ts`
- `src/lib/axios/interceptors.ts`

Add:

- API base URL
- token storage key
- request interceptor for bearer token
- response interceptor for unauthorized handling

Goal:

- centralize backend communication rules before building any feature

## 3. Stage 3: Add core shared types

Create:

- pagination types
- shared common types if needed

Then define per-feature DTOs in `features/*/types.ts`.

Reason:

- pages become easier to build once API data shapes are clear

## 4. Stage 4: Build auth first

Create:

- `features/auth/types.ts`
- `features/auth/api.ts`
- `context/AuthContext.tsx`
- `pages/LoginPage.tsx`
- `pages/RegisterPage.tsx`

Reason:

- most other screens depend on knowing the current user and role
- protected routing depends on auth

## 5. Stage 5: Build routing and access control

Create:

- `router/AppRouter.tsx`
- `router/ProtectedRoute.tsx`
- `router/RoleGuard.tsx`

Start simple:

- public login/register
- one protected home route

Then expand role-specific routes gradually.

## 6. Stage 6: Build the app shell

Create:

- `components/layout/AppLayout.tsx`
- `components/layout/Sidebar.tsx`
- `components/layout/Header.tsx`

Then define common global styles in `src/index.css`.

Reason:

- once the shell exists, new pages can drop into a consistent structure

## 7. Stage 7: Build common reusable primitives early

Before many pages, create:

- `Pagination`
- `SortableHeader`
- `StatusBadge`
- `PriorityBadge`
- `TaskStatusBadge`
- `TablePrimitives`

Why:

- many feature pages need the same table/list UI
- building these once avoids repeated ad hoc markup

## 8. Stage 8: Build the first real domain module

Best first module: incidents.

Why incidents first:

- core business entity
- simpler than reports
- creates a pattern reusable for tasks and admin screens

Build in this order:

1. `features/incidents/types.ts`
2. `features/incidents/api.ts`
3. `pages/incidents/IncidentsListPage.tsx`
4. `pages/incidents/IncidentCreatePage.tsx`
5. `pages/incidents/IncidentDetailPage.tsx`

## 9. Stage 9: Build tasks next

Once incidents work, build tasks.

Reason:

- tasks depend conceptually on incidents
- task creation links to incidents
- task details link back to incidents

Build:

1. task types
2. task API
3. tasks list
4. task create
5. task detail

At this point, the app already has its main business workflow.

## 10. Stage 10: Add notifications and shared live behavior

Create:

- notification types
- notification API
- `NotificationContext`
- notifications page
- header dropdown integration

This should come after auth because notifications depend on logged-in user state.

## 11. Stage 11: Add admin and manager modules

Build these after the core business path is stable:

- departments
- categories
- admin users
- manager users
- audit logs
- compliance
- manager analytics
- admin reports

Reason:

- these are more specialized
- they rely on the same shared table, form, and API patterns

## 12. Stage 12: Refine UX and edge cases

After all major screens exist, improve:

- loading states
- empty states
- error messages
- disabled states
- route guards
- form validation
- role-specific visibility

This project already uses that refinement pattern in many pages.

## 13. Personal mental checklist while recreating

Whenever building a new screen, ask:

1. What route should open this screen?
2. What role can access it?
3. What API file should own the backend calls?
4. What TypeScript types define the payload and response?
5. What state is truly local?
6. What state should be derived instead of stored?
7. Can I reuse an existing `common` component?
8. What are the loading, error, empty, and success states?

If you follow that checklist, your recreated version will stay close to the original design quality.

## 14. Final rebuild summary

The correct rebuild order is not "file by file in folder order". It is:

- bootstrap
- HTTP layer
- types
- auth
- routing
- layout
- reusable primitives
- core business modules
- shared live modules
- advanced admin modules
- UX polish

That sequence matches how the dependencies in this frontend actually work.
