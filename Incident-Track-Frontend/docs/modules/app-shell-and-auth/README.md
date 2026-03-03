# App Shell And Auth

This module covers how the frontend starts, how it knows who the user is, and how route access is enforced.

## Core files

- [main.tsx](../../../src/main.tsx)
- [App.tsx](../../../src/App.tsx)
- [AppRouter.tsx](../../../src/router/AppRouter.tsx)
- [ProtectedRoute.tsx](../../../src/router/ProtectedRoute.tsx)
- [RoleGuard.tsx](../../../src/router/RoleGuard.tsx)
- [AuthContext.tsx](../../../src/context/AuthContext.tsx)
- [AppLayout.tsx](../../../src/components/layout/AppLayout.tsx)
- [Sidebar.tsx](../../../src/components/layout/Sidebar.tsx)
- [Header.tsx](../../../src/components/layout/Header.tsx)
- [LoginPage.tsx](../../../src/pages/LoginPage.tsx)
- [RegisterPage.tsx](../../../src/pages/RegisterPage.tsx)
- [UnauthorizedPage.tsx](../../../src/pages/UnauthorizedPage.tsx)
- [NotFoundPage.tsx](../../../src/pages/NotFoundPage.tsx)

## Boot sequence

`main.tsx` is the real frontend entry point:

1. it imports global CSS
2. it attaches Axios interceptors
3. it mounts `AuthProvider`
4. it mounts `NotificationProvider`
5. it renders `<App />`

The Axios interceptor is important because it clears the saved token and redirects to `/login` whenever the backend returns `401`.

## Auth state

`AuthContext.tsx` owns:

- the JWT token from local storage
- the current user profile
- loading state during app boot
- helper methods: `login`, `logout`, `refreshProfile`

The key design choice is that login is a two-step process:

1. call `authApi.login(...)` to get the JWT
2. immediately call `viewProfile()` to fetch the full user object used by the rest of the UI

That keeps route guards simple because they can rely on both `token` and `user`.

## Route protection

- `ProtectedRoute.tsx`
  Blocks all protected pages until auth boot finishes, then redirects anonymous users to `/login`.
- `RoleGuard.tsx`
  Handles role-based route checks and redirects unauthorized users to `/unauthorized`.

`AppRouter.tsx` groups routes into nested sections, so the access model is easy to audit in one file.

## App shell

The shared shell is composed from:

- `AppLayout.tsx`
  Desktop + mobile shell with a collapsible sidebar and a scrollable main content area.
- `Sidebar.tsx`
  Role-aware navigation. It only shows links the current user can access.
- `Header.tsx`
  Greeting banner plus the notification bell dropdown.

This shell wraps every protected page, so changes to navigation or top-level layout usually start in these three files.

## Practical notes

- The saved token key is `it_token`.
- Public routes are intentionally minimal: only login and register.
- The current routed landing page is `/` mapped to `HomePage.tsx`, not the older role-specific dashboard components.
