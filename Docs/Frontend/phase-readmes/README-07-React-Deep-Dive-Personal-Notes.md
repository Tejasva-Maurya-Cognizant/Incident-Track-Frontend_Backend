# Frontend Phase 7: React Deep Dive Personal Notes

This file is intentionally written like personal study notes. The goal is to understand React itself using examples from this project, not just memorize definitions.

## 1. My mental model of React in this project

The simplest way to understand this frontend is:

- React is responsible for rendering UI based on state.
- When state changes, React re-renders the component tree that depends on that state.
- Components are just functions, but they are not "normal" utility functions. They participate in React's render lifecycle.

In this project:

- pages render route-level screens
- contexts provide shared state
- reusable components reduce repeated UI
- hooks manage state, effects, and derived logic

So the core idea is:

"This app is a tree of function components that re-render when local state, context state, or route state changes."

## 2. JSX in practical terms

JSX looks like HTML, but it is JavaScript syntax that describes UI.

Example idea:

```tsx
return <TaskStatusBadge status={task.status} />;
```

This is not actually raw HTML. It is React element creation syntax.

Important practical understanding:

- JSX can embed JavaScript expressions inside `{}`.
- JSX can conditionally render fragments.
- JSX can map arrays into repeated UI.

Examples used heavily in this project:

- conditional rendering with `&&`
- ternary rendering with `condition ? a : b`
- array `.map()` for lists and tables

## 3. Rendering and re-rendering

One of the most important beginner concepts:

- A component function runs again when React decides it must re-render.
- Re-render does not mean full page reload.
- Re-render means React recalculates the UI description.

Common triggers in this project:

- `setState(...)`
- context value changes
- route changes
- parent re-render

Example:

In `NotificationsPage`, changing the selected tab with:

```tsx
setTab("UNREAD");
```

causes the component to re-run and display a filtered list.

## 4. Why hooks exist

Hooks let function components use React features like:

- state
- lifecycle-like behavior
- context
- refs

Without hooks, function components would just be "input -> JSX output" functions.

Hooks give them memory and side-effect behavior.

## 5. Rules of hooks

These are important for interviews and debugging:

- Call hooks only at the top level of a component or custom hook.
- Do not call hooks inside loops, conditions, or nested ordinary functions.
- Hook call order must stay consistent between renders.

Why:

React tracks hook state by call order. If the order changes, state mapping breaks.

This project generally follows that correctly.

## 6. Deep note on `useState`

`useState` gives a component local memory.

Syntax:

```tsx
const [value, setValue] = useState(initialValue);
```

The important part is not just syntax. The important part is behavior:

- `value` is the current state for this render
- `setValue` schedules a re-render with new state
- state survives between renders

### Where `useState` is heavily used here

- login and registration forms
- list filters
- modal open/close state
- selected tab
- API loading flags
- API error messages
- fetched item arrays
- selected records for detail/edit

### Good practical examples in this project

`AuthContext`:

- token
- user
- loading

`TaskCreatePage`:

- form fields
- field validation errors
- saving state

`AppLayout`:

- mobile sidebar open/closed

### Key interview point

`useState` is for data that changes over time and should affect rendering.

## 7. Deep note on `useEffect`

`useEffect` is the hook for side effects.

A side effect means work that goes beyond calculating JSX, such as:

- HTTP requests
- subscriptions
- timers
- DOM event listeners
- local storage reads/writes

Syntax pattern:

```tsx
useEffect(() => {
  // side effect
  return () => {
    // cleanup (optional)
  };
}, [dependencies]);
```

### How to think about dependencies

The dependency array answers:

"When should React run this effect again?"

- `[]` means: run once after initial mount
- `[a, b]` means: rerun when `a` or `b` changes
- no dependency array means: run after every render

### Examples from this project

`AuthContext`:

- bootstraps login state on mount

`IncidentsListPage`:

- refetches data when page params or filters change

`Header`:

- attaches outside-click listener only when dropdown is open

`NotificationContext`:

- creates and cleans up SSE connection

### Why cleanup matters

Cleanup prevents:

- memory leaks
- duplicate listeners
- stale connections
- unexpected repeated events

This project uses cleanup correctly in important places, especially for notification SSE and DOM listeners.

## 8. Deep note on `useMemo`

`useMemo` is for memoizing a computed value.

Important detail:

- It does not "make things fast" by magic.
- It is useful when a derived value should not be recalculated unless inputs change.

Syntax:

```tsx
const result = useMemo(() => computeSomething(a, b), [a, b]);
```

### Good uses in this project

- deriving filtered arrays
- building unique category names
- memoizing auth context value
- keeping greeting text stable

### Why it is useful in forms and lists

For example, in `IncidentCreatePage`, category-related derived values are based on `categories` and `parentCategory`.

That means:

- base state stays clean
- derived state is not duplicated manually
- the UI remains easier to reason about

### Interview caution

Do not say "`useMemo` always improves performance." That is weak and often wrong.

Better answer:

"`useMemo` memoizes derived values so they are recomputed only when their dependencies change. In this project it is mainly used to keep derived UI logic readable and avoid unnecessary recalculation."

## 9. Deep note on `useCallback`

`useCallback` memoizes a function reference.

Syntax:

```tsx
const fn = useCallback(() => {
  ...
}, [deps]);
```

### Important distinction

- `useMemo` memoizes a value
- `useCallback` memoizes a function

### Why it matters here

In many pages, `load()` is used in effects:

```tsx
const load = useCallback(async () => {
  ...
}, [someDeps]);
```

Then:

```tsx
useEffect(() => {
  load();
}, [load]);
```

This pattern helps keep dependency handling sane.

Without `useCallback`, `load` would be a new function on each render, which can cause effects to run more often than intended.

### Good practical note

Do not use `useCallback` everywhere. Use it when function identity matters. This project uses it mostly in the right places: effects, context values, and shared handlers.

## 10. Deep note on `useRef`

`useRef` stores a mutable value that persists between renders but does not trigger re-render when changed.

Syntax:

```tsx
const ref = useRef(initialValue);
```

### Two common uses in this project

1. DOM access
2. mutable instance-like storage

### DOM access example

In `Header`, a ref points to the dropdown wrapper so outside clicks can be detected.

### Mutable storage example

In `NotificationContext`, refs store:

- current `EventSource`
- retry timeout
- retry count
- connection flags

These values must survive across renders, but changing them should not redraw the UI.

That is exactly why `useRef` is correct there.

## 11. Context in deeper terms

Context is not "global state management" in the Redux sense by default. It is a way to pass shared values through the tree without manual prop drilling.

In this project:

- `AuthContext` provides user/auth data
- `NotificationContext` provides notification data and actions

### Why this project uses context

Because many unrelated parts of the app need the same shared data:

- route guards need auth
- sidebar needs user and logout
- header needs user and notifications
- notifications page needs notifications

Context removes the need to thread all that through parent props.

### Important caution

Context is useful, but if its value changes often, many consumers re-render. That is one reason `AuthContext` memoizes its value.

## 12. Controlled forms in this project

Most forms in this frontend are controlled forms.

That means:

- input value comes from React state
- input updates call `setState`

Pattern:

```tsx
<input
  value={email}
  onChange={(e) => setEmail(e.target.value)}
/>
```

Why this is useful:

- validation is easier
- submit payload is already in state
- UI can react immediately to changes

You see this in:

- `LoginPage`
- `RegisterPage`
- `IncidentCreatePage`
- `TaskCreatePage`
- admin modals

## 13. Derived state vs stored state

This is a very important React design concept.

Bad pattern:

- store everything separately, even if some values can be calculated from others

Better pattern:

- store the true source of state
- derive the rest with code or `useMemo`

This project does that in many places:

- filtered lists are derived from raw data + filter values
- role booleans are derived from `user?.role`
- unread count is derived from notifications array

That keeps state smaller and more reliable.

## 14. Conditional rendering patterns used here

This project uses several common patterns:

### Simple condition with `&&`

```tsx
{err && <div>{err}</div>}
```

### Ternary

```tsx
{loading ? <Loading /> : <Content />}
```

### Multi-branch logic

Common in tables:

- loading
- error
- empty
- data

This is a very standard and good UI pattern.

## 15. List rendering and keys

When rendering arrays in React:

- use `.map()`
- each rendered item needs a stable `key`

Example from this app:

```tsx
{recentTasks.map((t) => (
  <TableBodyRow key={t.taskId}>
    ...
  </TableBodyRow>
))}
```

Why key matters:

- helps React track item identity
- improves update correctness
- avoids weird UI behavior during list changes

In this project, backend IDs are used as keys in most places, which is correct.

## 16. Event handling in this frontend

Examples of events:

- `onClick`
- `onChange`
- `onSubmit`
- `onKeyDown`

Key note:

React event handlers are functions passed to JSX props.

Example:

```tsx
const onSubmit = async (e: React.FormEvent) => {
  e.preventDefault();
  ...
};
```

This is seen across forms in the project.

## 17. API-driven React screens: the repeated lifecycle

A lot of pages in this project follow the same lifecycle:

1. initialize local state
2. run `useEffect` or call `load()`
3. show loading UI
4. receive API response
5. update state
6. re-render with actual data
7. optionally submit mutations
8. refetch or update local state again

This pattern is visible in:

- incidents list
- tasks list
- admin users
- reports
- notifications

If you understand this lifecycle, the whole codebase becomes much easier to read.

## 18. Why React fits this project well

This frontend has:

- many dynamic lists
- role-based UI changes
- forms
- conditional panels
- shared top-level state
- real-time updates

React fits well because it is good at:

- describing UI as a function of state
- composing small reusable pieces
- handling frequent UI state changes
- keeping business logic close to the screen that needs it

## 19. Personal short summary to remember

If I had to reduce the whole frontend to one mental note:

"This is a route-based React app where pages fetch typed backend data through feature APIs, store screen state with hooks, share auth and notifications through context, and render reusable UI primitives inside a common app layout."

That one sentence is enough to anchor the entire architecture in memory.
