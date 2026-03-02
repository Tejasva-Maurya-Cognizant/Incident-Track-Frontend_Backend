# Frontend Phase 3: Entry Flow, Routing, Authentication, and Authorization

This file explains how the app moves from startup into protected screens.

## 1. Router-first design

The project uses `createBrowserRouter` and `RouterProvider` from `react-router-dom`.

This means:

- the URL controls which page is shown
- navigation is client-side
- nested wrappers handle auth and access rules

Core pattern:

```tsx
export default function AppRouter() {
  return <RouterProvider router={router} />;
}
```

## 2. Public routes

These routes do not require login:

- `/login`
- `/register`

They render `LoginPage` and `RegisterPage`.

## 3. Protected routes

Private screens are wrapped like this:

```tsx
{
  element: <ProtectedRoute />,
  children: [
    {
      element: <AppLayout />,
      children: [
        { path: "/", element: <HomePage /> },
        ...
      ],
    },
  ],
}
```

The layered meaning is:

1. user must be authenticated
2. then the layout is shown
3. then the actual page is rendered inside it

## 4. `ProtectedRoute`

`ProtectedRoute` reads auth state from context:

```tsx
const { isLoading, isAuthenticated } = useAuth();
if (isLoading) return null;
return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
```

Meaning:

- wait while auth is initializing
- if authenticated, continue to child routes
- otherwise redirect to login

`<Outlet />` is the placeholder for nested route content.

## 5. `RoleGuard`

Some routes are role-specific.

Example:

```tsx
<RoleGuard allow={["ADMIN"]} />
```

Its logic is:

- if role is missing, redirect to `/login`
- if role is allowed, render children
- otherwise redirect to `/unauthorized`

This is authorization. Authentication and authorization are different:

- authentication: who the user is
- authorization: what the user can access

## 6. How `AuthContext` works

`AuthContext.tsx` stores:

- `token`
- `user`
- `isLoading`
- `isAuthenticated`
- `role`
- `login`
- `logout`
- `refreshProfile`

Important state:

```tsx
const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY));
const [user, setUser] = useState<UserResponseDto | null>(null);
const [isLoading, setIsLoading] = useState(true);
```

## 7. Why `useState` is used here

`useState` stores values that affect rendering and must persist between renders.

In auth:

- token changes change security state
- user changes change role-aware UI
- loading changes control route guards

## 8. Why `useEffect` is used here

The auth provider runs a startup effect:

```tsx
useEffect(() => {
  const boot = async () => {
    try {
      if (token) await refreshProfile();
    } catch {
      logout();
    } finally {
      setIsLoading(false);
    }
  };
  boot();
}, []);
```

Purpose:

- read existing login state on app start
- if token exists, fetch profile
- if token is invalid, clear auth
- finally stop loading

This is how login survives a page refresh.

## 9. Why `useMemo` is used in auth

The context value is memoized:

```tsx
const value = useMemo<AuthState>(
  () => ({
    token,
    user,
    isLoading,
    isAuthenticated: !!token && !!user,
    role: user?.role ?? null,
    login,
    logout,
    refreshProfile,
  }),
  [token, user, isLoading]
);
```

Purpose:

- reduce unnecessary context consumer re-renders
- keep the provider value stable until important state changes

## 10. Login flow end-to-end

`LoginPage.tsx` does this:

1. collects email and password
2. prevents default form submit
3. calls `login()`
4. auth API returns token
5. token is stored
6. profile is fetched
7. page redirects to `/`

Core line:

```tsx
await login({ email, password });
window.location.href = "/";
```

## 11. Registration flow

`RegisterPage.tsx`:

- manages local form state
- loads departments on mount with `useEffect`
- submits data through `authApi.register()`
- redirects to login after success

This page is a standard React typed form workflow.

## 12. Axios interceptors and forced logout

The app uses centralized Axios interceptors.

Request interceptor:

- reads token from `localStorage`
- adds `Authorization: Bearer <token>`

Response interceptor:

- watches for `401`
- clears token
- redirects to `/login`

This avoids duplicating auth-header logic in every API file.

## 13. Interview-ready summary

A strong explanation is:

"The frontend stores auth state in a React Context, initializes it on app load by reading a token from local storage and fetching the current profile, protects private routes with a `ProtectedRoute`, restricts role-specific routes with `RoleGuard`, and uses Axios interceptors to attach the bearer token and handle unauthorized responses globally."
