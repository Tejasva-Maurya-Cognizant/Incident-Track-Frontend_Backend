# Frontend Phase 4: Shared UI System, Contexts, and Data Access Layer

This file explains the reusable infrastructure that business pages depend on.

## 1. Global styling strategy

The project uses Tailwind CSS plus a small shared design system in `src/index.css`.

Important CSS variables:

- `--brand`
- `--brandHover`
- `--bg`
- `--card`
- `--border`
- `--inputBorder`
- `--app-shell-max`
- `--page-panel-max`

This keeps colors, spacing behavior, and layout sizing consistent.

## 2. Reusable CSS helper classes

Defined in `@layer components`:

- `.card`
- `.page-panel`
- `.input`
- `.btn-primary`
- `.sticky-bar`
- `.sticky-thead`

This is a balanced approach:

- Tailwind utilities for most styling
- shared classes for repeated UI patterns

## 3. `AppLayout`

`AppLayout.tsx` provides:

- desktop sidebar
- mobile slide-in sidebar
- header
- main scrollable content area

It uses:

```tsx
const [sidebarOpen, setSidebarOpen] = useState(false);
```

This is a direct example of local UI state controlling responsive behavior.

## 4. `Sidebar`

The sidebar:

- reads `user` and `logout` from `useAuth()`
- reads `unreadCount` from `useNotifications()`
- filters links by role
- opens the `ProfilePanel`

Key pattern:

```tsx
const allLinks = [...].filter((l) => role && l.roles.includes(role));
```

This makes navigation role-aware at render time.

## 5. `Header`

The header:

- shows a greeting
- shows username and role
- opens a notifications dropdown
- supports mobile menu toggle

It uses multiple hooks together:

- `useMemo` for greeting text
- `useState` for dropdown visibility
- `useRef` for dropdown DOM reference
- `useEffect` for outside-click cleanup logic

Outside-click pattern:

```tsx
useEffect(() => {
  if (!dropOpen) return;
  const handler = (e: MouseEvent) => {
    if (dropRef.current && !dropRef.current.contains(e.target as Node)) {
      setDropOpen(false);
    }
  };
  document.addEventListener("mousedown", handler);
  return () => document.removeEventListener("mousedown", handler);
}, [dropOpen]);
```

This is a common real-world React pattern for dropdowns and modals.

## 6. The API layer (`features/*/api.ts`)

Each feature has a dedicated API file that wraps backend calls.

Example pattern:

```tsx
const res = await api.get<PagedResponse<TaskResponseDTO>>(`${BASE}/paged`, {
  params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
});
return res.data;
```

Benefits:

- pages stay focused on UI behavior
- endpoints are centralized
- types stay attached to requests and responses

## 7. Why `types.ts` matters

Feature `types.ts` files define:

- DTO interfaces
- request payload shapes
- status/role union types

In this project, TypeScript helps:

- catch invalid property access
- make API contracts explicit
- improve autocomplete
- make refactoring safer

## 8. `NotificationContext`

`NotificationContext.tsx` is one of the most advanced shared files.

It handles:

- loading notifications
- unread counts
- marking one as read
- marking all as read
- live updates through Server-Sent Events (SSE)

## 9. Hooks used in notification context

### `useState`

Stores notification data and loading flags.

### `useRef`

Stores mutable non-UI values:

- current `EventSource`
- retry timer
- retry count
- connection guard flag

This is exactly what `useRef` is for: values that should persist without causing re-renders.

### `useCallback`

Used for stable action functions:

- `refresh`
- `markAsRead`
- `markAllRead`

### `useEffect`

Used for:

- initial notification fetch
- SSE connection lifecycle and cleanup

## 10. Why SSE is used

SSE allows the backend to push notifications to the browser.

Benefits:

- near real-time updates
- less waste than constant polling
- simple browser-native API

This project also includes exponential backoff retry logic, which is a solid production-minded detail.

## 11. Important shared components

Reusable `common` components worth learning:

- `Pagination`
- `SortableHeader`
- `StatusBadge`
- `PriorityBadge`
- `TaskStatusBadge`
- `TablePrimitives`
- `ProfilePanel`
- `ComboBox`
- `Select`
- `ModalWindow`

These components reduce repeated UI code across pages.

## 12. Interview-ready summary

A strong explanation is:

"The frontend separates data access into feature-specific API modules, stores cross-cutting state in React Context, centralizes HTTP behavior with Axios interceptors, and builds route pages on top of reusable layout and UI primitives. This keeps the page layer focused on user workflows instead of infrastructure concerns."
