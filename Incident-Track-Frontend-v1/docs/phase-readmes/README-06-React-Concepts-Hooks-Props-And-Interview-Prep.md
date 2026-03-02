# Frontend Phase 6: React Concepts, Hooks, Props, and Interview Prep

This file is meant to be a detailed bridge between React theory and the exact way this project uses React. Think of it as interview notes plus project understanding notes.

## 1. How to think about React in this project

React is not "a page builder". In this frontend, React is the runtime that:

- takes component functions
- runs them to produce UI
- re-runs them when state changes
- updates only the parts of the DOM that need to change

That means the core rule of the frontend is:

"UI is a function of current state."

In this project, the state comes from three main places:

- local component state (`useState`)
- shared context state (`useAuth`, `useNotifications`)
- route state (`useParams`, `useSearchParams`, current path)

## 2. Components in this project

A component is a function that returns JSX.

Example:

```tsx
export default function App() {
  return <AppRouter />;
}
```

But in practice, different components in this project play different roles:

- page components: route-level screens like `IncidentsListPage`
- layout components: shell wrappers like `AppLayout`, `Header`, `Sidebar`
- guard components: route protection logic like `ProtectedRoute`, `RoleGuard`
- reusable UI components: badges, tables, pagination
- modal/panel components: things like `ProfilePanel`

Understanding the type of component helps you predict what kind of state and logic it should contain.

## 3. Props: how data moves from parent to child

Props are inputs a parent gives to a child component.

They are how React composes UI cleanly.

Example from this project:

```tsx
export const RoleGuard: React.FC<{ allow: UserRole[] }> = ({ allow }) => {
  ...
};
```

The parent route tree passes `allow`, and the guard uses it to decide whether to render children.

Another common pattern:

```tsx
function NotificationDropdown({ onClose }: { onClose: () => void }) {
  ...
}
```

`onClose` is a callback prop. The parent owns the open/closed state, and the child asks the parent to update it by calling `onClose()`.

That pattern appears throughout React:

- parent owns state
- child receives data and callbacks
- child never directly mutates parent state

## 4. State: what changes over time

State is data that belongs to a component and can change after the first render.

In this project, state is used for:

- form fields
- loading flags
- error messages
- fetched data arrays
- selected tabs
- selected filters
- selected records for editing/viewing
- visual UI states like dropdowns and sidebars

Example:

```tsx
const [email, setEmail] = useState("");
const [password, setPassword] = useState("");
```

These values:

- persist across re-renders
- update through setters
- drive the rendered UI

## 5. `useState` in real project terms

`useState` is the most used hook in this app because most UI behavior is local and interactive.

### Common `useState` categories in this codebase

- form state: email, password, description, selected status
- request state: loading, saving, updating
- error state: human-readable failure messages
- list state: arrays of incidents, tasks, users, notifications
- UI state: tab selection, modal open state, sidebar visibility

### Why `useState` fits these cases

Because these values:

- change due to user actions or API results
- should trigger a re-render
- belong only to one screen or one component

### Examples

`LoginPage`:

- email
- password
- loading
- error

`AppLayout`:

- mobile sidebar open/close

`TaskDetailPage`:

- current task
- update status selection
- update success/failure flags

Interview-friendly definition:

"`useState` stores local component state and causes the component to re-render when the setter updates that state."

## 6. `useEffect`: where side effects belong

`useEffect` is used for work that should happen after rendering, not during pure rendering.

In this project, that includes:

- fetching data from APIs
- bootstrapping auth from local storage
- attaching event listeners
- opening and closing live connections
- cleaning up subscriptions/timers/listeners

### Important mental model

Rendering should describe UI.

Effects should perform actions.

If you put too much non-UI work directly in render logic, you create repeated execution bugs.

### Example: auth bootstrap

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

This effect runs once when the provider mounts and restores login state.

### Example: list refetching

```tsx
useEffect(() => {
  load(params);
}, [params, load]);
```

This means:

- when pagination/filter state changes
- call the async loader again
- update the screen

### Example: event listener cleanup

In `Header`, the dropdown outside-click listener is added when open and removed in cleanup. That is correct effect usage because it ties the listener lifecycle to UI state.

Interview-friendly definition:

"`useEffect` runs side effects after rendering and can re-run when dependencies change. It is used for data fetching, subscriptions, and DOM integration."

## 7. Dependency arrays: a concept you must understand well

A lot of React bugs come from misunderstanding dependencies.

Dependency array meanings:

- `[]`: run once after mount
- `[a, b]`: rerun when `a` or `b` changes
- omitted: run after every render

In practice:

- if an effect uses a value, that value usually belongs in the dependency array
- if a function is recreated every render, it may retrigger effects unless stabilized

This is one reason this project uses `useCallback` for many `load()` functions.

## 8. `useMemo`: memoized derived values

`useMemo` stores the result of a calculation and recomputes it only when dependencies change.

It is used here for:

- filtered lists
- derived category lists
- auth context value object
- stable greeting value in header

Example:

```tsx
const filteredItems = useMemo(
  () => severity ? items.filter((i) => i.calculatedSeverity === severity) : items,
  [items, severity]
);
```

This is good because:

- the source of truth remains `items` and `severity`
- filtered output is derived, not separately stored
- render logic stays cleaner

### Important interview nuance

Do not describe `useMemo` as a universal performance tool.

Better:

"`useMemo` is used to memoize derived values so they are only recalculated when their dependencies change. In this project it also helps keep derived UI logic readable."

## 9. `useCallback`: stable function references

`useCallback` memoizes a function reference.

This is used in the project mostly for:

- `load()` functions on list pages
- notification actions in context
- refresh handlers passed through shared state

Example:

```tsx
const load = useCallback(async (p: PageParams) => {
  ...
}, [statusFilter, isEmployee, isManager]);
```

Why it helps:

- effects depending on `load` do not re-run unnecessarily
- function identity stays stable until dependencies actually change

### Important note

You should not use `useCallback` everywhere by default. This project uses it mainly where function stability matters, which is the right idea.

## 10. `useRef`: persistent mutable values without re-render

`useRef` is used when you need to keep a value between renders but changing it should not redraw the UI.

This project uses `useRef` in two classic ways:

### 1. DOM references

Example:

- header dropdown wrapper element
- profile/dialog references

This supports behaviors like outside-click detection and focus-related logic.

### 2. Mutable runtime references

In `NotificationContext`, refs store:

- active `EventSource`
- retry timeout
- retry count
- connection state flags

These are not presentation state. They are runtime control values.

If these were in `useState`, the app would re-render for internal connection bookkeeping, which is unnecessary.

Interview-friendly definition:

"`useRef` stores mutable values that persist across renders without causing re-renders. It is used for DOM access and instance-like mutable state."

## 11. Context: global shared state without prop drilling

Context is used when unrelated parts of the tree need access to the same shared values.

This project has two main contexts:

- `AuthContext`
- `NotificationContext`

### Why `AuthContext` is needed

Many parts of the app need current user and auth state:

- route guards
- sidebar
- header
- home page
- role-restricted screens

### Why `NotificationContext` is needed

Many parts need notification data:

- header bell and dropdown
- sidebar unread badge
- notifications page

Without context, those values would have to be passed through many intermediate components that do not actually use them.

That problem is called prop drilling.

## 12. Controlled forms: the dominant form style here

This frontend mostly uses controlled inputs.

Pattern:

```tsx
<input
  value={email}
  onChange={(e) => setEmail(e.target.value)}
/>
```

This means:

- React state is the source of truth
- the input displays that state
- typing updates that state

Why this is useful:

- validation becomes straightforward
- the submit payload is already in state
- form errors can be shown immediately

You can see this across:

- login
- register
- incident creation
- task creation
- edit modals

## 13. Event handling in this codebase

Common React events used here:

- `onClick`
- `onChange`
- `onSubmit`
- `onKeyDown`

Example:

```tsx
const onSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  ...
};
```

The important part is:

- events are handled through functions
- default browser behavior is often prevented for SPA forms
- React state and router navigation then take over

## 14. Conditional rendering patterns you should recognize

This project uses standard React conditional rendering techniques.

### `&&` for simple optional UI

```tsx
{err && <div>{err}</div>}
```

### Ternary for two-branch rendering

```tsx
{loading ? <LoadingState /> : <Content />}
```

### Multi-branch rendering

Common in list pages:

- loading
- error
- empty
- actual rows

This is the correct practical way to build data-driven screens.

## 15. Lists, `.map()`, and keys

List rendering happens constantly in this app:

- tables
- dropdown items
- quick actions
- route sections

Pattern:

```tsx
{recentTasks.map((t) => (
  <TableBodyRow key={t.taskId}>
    ...
  </TableBodyRow>
))}
```

Why `key` matters:

- it gives React stable identity for list items
- helps React update efficiently and correctly
- prevents bugs during list changes

This project mostly uses backend IDs as keys, which is the right approach.

## 16. Derived state vs stored state

This is one of the most important design concepts in good React code.

### Stored state

Things that must be remembered and can change independently:

- current page number
- current filter
- fetched data array

### Derived state

Things that can be calculated from existing state:

- unread notification count
- filtered task list
- boolean flags like `isManager`

Examples in this project:

- `unreadCount` is derived from the notifications array
- role-based booleans are derived from `user?.role`
- filtered arrays are derived from data plus filter values

This keeps the state model smaller and less error-prone.

## 17. Router hooks used in this frontend

### `useNavigate`

Used for programmatic navigation after actions:

```tsx
navigate(`/tasks/${task.taskId}`);
```

### `useParams`

Used to read route parameters:

```tsx
const { id } = useParams();
```

### `useSearchParams`

Used to read query strings like:

- `/tasks/create?incidentId=12`
- `/incidents/20?fromTask=true`

These hooks are essential to the cross-screen flows in incidents and tasks.

## 18. Async UI lifecycle used across the app

Most pages follow the same async lifecycle:

1. initialize local state
2. trigger `load()` or `onSubmit()`
3. set loading flag
4. call API
5. update state on success
6. store error on failure
7. stop loading in `finally`
8. re-render with the new UI state

This pattern appears in almost every real feature page.

Once you recognize it, the project becomes much easier to read.

## 19. How to explain this project in a React interview

A strong answer:

"This frontend is built as a React 19 + TypeScript SPA. It uses route-level page components for each screen, Context for shared auth and notification state, typed feature API modules for backend communication, and common UI primitives for repeated patterns like tables, pagination, badges, and modals. Local component behavior is managed with `useState`, data fetching and subscriptions use `useEffect`, derived values use `useMemo`, stable handlers use `useCallback`, and `useRef` is used for DOM access and connection-level mutable values."

## 20. Personal memory summary

If you want one compact mental note to remember:

"React in this project is mainly used to connect state to UI: local state for screen behavior, context for cross-cutting state, effects for async work, and reusable components for consistent rendering."

If you can explain that sentence with examples from the actual files, you understand the frontend at a strong practical level.
