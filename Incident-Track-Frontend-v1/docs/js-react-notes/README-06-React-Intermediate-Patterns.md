# React Intermediate Patterns

This file moves beyond basic syntax and basic hooks. It focuses on how real React applications are structured and how to think more like an actual frontend developer.

## 1. Thinking in components

A beginner often sees a page as one large block.

A stronger React developer sees:

- layout parts
- data parts
- reusable UI parts
- state owners
- pure display children

Example page breakdown:

- page component
- header section
- filters section
- list/table
- pagination
- modal

This is called thinking in components.

## 2. Lifting state up

Sometimes two child components need the same data.

When that happens, move the state to their nearest common parent.

Example:

```jsx
function SearchBar({ search, setSearch }) {
  return (
    <input
      value={search}
      onChange={(e) => setSearch(e.target.value)}
      placeholder="Search"
    />
  );
}

function ResultList({ items, search }) {
  const filtered = items.filter((item) =>
    item.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <ul>
      {filtered.map((item) => (
        <li key={item}>{item}</li>
      ))}
    </ul>
  );
}

function App() {
  const [search, setSearch] = useState("");
  const items = ["React", "JavaScript", "TypeScript"];

  return (
    <div>
      <SearchBar search={search} setSearch={setSearch} />
      <ResultList items={items} search={search} />
    </div>
  );
}
```

Why this matters:

- there is one source of truth
- sibling components stay in sync

## 3. Single source of truth

This is a major frontend design principle.

It means:

- keep one authoritative place for a piece of state
- derive the rest from it

Bad design:

- the same data is stored separately in multiple places

Better design:

- one component or context owns the state
- other components receive it through props or context

This reduces bugs and makes updates predictable.

## 4. Derived state

Derived state is data calculated from other state, instead of stored separately.

Example:

```jsx
const [tasks, setTasks] = useState([
  { id: 1, done: true },
  { id: 2, done: false },
]);

const completedCount = tasks.filter((task) => task.done).length;
```

`completedCount` should usually be derived, not stored separately.

Why:

- avoids duplication
- avoids synchronization bugs

## 5. Data fetching patterns

One of the most common real React tasks is loading data from APIs.

Basic pattern:

1. local state for data
2. local state for loading
3. local state for error
4. async function to fetch
5. `useEffect` to trigger initial load

Example:

```jsx
function Posts() {
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadPosts = async () => {
      setLoading(true);
      setError("");

      try {
        const response = await fetch("/api/posts");
        const data = await response.json();
        setPosts(data);
      } catch (err) {
        setError("Failed to load posts");
      } finally {
        setLoading(false);
      }
    };

    loadPosts();
  }, []);

  if (loading) return <p>Loading...</p>;
  if (error) return <p>{error}</p>;

  return (
    <ul>
      {posts.map((post) => (
        <li key={post.id}>{post.title}</li>
      ))}
    </ul>
  );
}
```

## 6. Loading, error, empty, success states

This is a core UI pattern.

Many pages need four states:

- loading
- error
- empty
- success

Example:

```jsx
if (loading) return <p>Loading...</p>;
if (error) return <p>{error}</p>;
if (items.length === 0) return <p>No items found</p>;

return <List items={items} />;
```

This is a very common and good pattern in real apps.

## 7. Forms and validation patterns

Real forms need more than just input state.

They often need:

- field values
- field-level errors
- form-level error
- submission loading state
- success state

Simple example:

```jsx
function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    if (!email.trim() || !password.trim()) {
      setError("Email and password are required");
      return;
    }

    setError("");
    console.log("Submit", { email, password });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input value={email} onChange={(e) => setEmail(e.target.value)} />
      <input value={password} onChange={(e) => setPassword(e.target.value)} />
      {error && <p>{error}</p>}
      <button type="submit">Login</button>
    </form>
  );
}
```

## 8. Updating arrays and objects in React state

React state should be updated immutably.

### Add item to array

```jsx
setTasks([...tasks, newTask]);
```

### Remove item from array

```jsx
setTasks(tasks.filter((task) => task.id !== idToRemove));
```

### Update one item in array

```jsx
setTasks(
  tasks.map((task) =>
    task.id === idToUpdate ? { ...task, done: true } : task
  )
);
```

### Update object

```jsx
setUser({ ...user, name: "New Name" });
```

This is one of the most important practical React skills.

## 9. Parent-child communication patterns

### Parent to child

Use props.

### Child to parent

Pass a callback prop.

Example:

```jsx
function Child({ onAdd }) {
  return <button onClick={() => onAdd("New Item")}>Add</button>;
}

function Parent() {
  const [items, setItems] = useState([]);

  const handleAdd = (item) => {
    setItems([...items, item]);
  };

  return <Child onAdd={handleAdd} />;
}
```

This is the standard way child components influence parent-owned state.

## 10. State colocation

State colocation means:

- keep state as close as possible to where it is used
- do not move state higher than necessary

Good:

- local dropdown open state inside dropdown component

Move upward only when:

- multiple components need the same state
- parent must coordinate it

This is the balance between local state and lifted state.

## 11. Context as shared app-level state

Context is useful when many components need the same data.

Good examples:

- current user
- auth token
- theme
- notifications

Use context when prop drilling becomes annoying or awkward.

Do not use context for every little toggle or input field.

## 12. Custom hooks for reusable logic

Custom hooks are extremely useful in real projects.

They let you extract repeated logic from components.

Example:

```jsx
function useFetch(url) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError("");

      try {
        const response = await fetch(url);
        const result = await response.json();
        setData(result);
      } catch (err) {
        setError("Fetch failed");
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [url]);

  return { data, loading, error };
}
```

Use it:

```jsx
function Users() {
  const { data, loading, error } = useFetch("/api/users");
  ...
}
```

This improves reuse and keeps components cleaner.

## 13. React rendering optimization basics

You do not need to optimize everything early, but you should understand the basics.

### `React.memo`

Prevents re-render of a child component when props are unchanged.

### `useMemo`

Memoizes values.

### `useCallback`

Memoizes function references.

Important rule:

Optimize when there is a real reason:

- expensive calculations
- unnecessary child re-renders
- large lists

Do not optimize blindly.

## 14. Reconciliation and re-rendering

When state changes:

- React re-runs the component
- compares new UI description with previous one
- updates only the necessary DOM parts

That comparison process is part of what people refer to when discussing React's rendering model and reconciliation.

You do not manually update each DOM node. React handles that based on the new state.

## 15. Common real-world component patterns

### Container and presentational split

Container component:

- handles data loading and state

Presentational component:

- mainly displays UI from props

This is not a strict rule, but it is a useful design pattern.

### Reusable primitives

Examples:

- button
- modal
- badge
- table row
- input wrapper

These help maintain consistency across the app.

## 16. React app structure at a high level

A typical real app often has:

- entry point
- app root
- routes
- pages
- reusable components
- hooks
- shared context
- API layer
- utility functions

That is why React is more than just writing JSX in one file.

## 17. Common mistakes in intermediate React

- storing too much duplicate state
- mutating state directly
- putting everything into one huge component
- overusing context
- overusing `useEffect`
- forgetting cleanup for subscriptions/listeners
- creating unstable keys
- using indexes as keys in dynamic lists
- not separating fetching logic from display logic

## 18. Mini implementation example: searchable task list

```jsx
import { useMemo, useState } from "react";

function TaskList() {
  const [search, setSearch] = useState("");
  const [tasks] = useState([
    { id: 1, title: "Learn JS", done: true },
    { id: 2, title: "Learn React", done: false },
    { id: 3, title: "Build App", done: false },
  ]);

  const filteredTasks = useMemo(() => {
    const q = search.toLowerCase();
    return tasks.filter((task) => task.title.toLowerCase().includes(q));
  }, [tasks, search]);

  return (
    <div>
      <input
        value={search}
        onChange={(e) => setSearch(e.target.value)}
        placeholder="Search tasks"
      />

      {filteredTasks.length === 0 ? (
        <p>No tasks found</p>
      ) : (
        <ul>
          {filteredTasks.map((task) => (
            <li key={task.id}>
              {task.title} - {task.done ? "Done" : "Pending"}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

This one example shows:

- local state
- controlled input
- derived filtered data
- `useMemo`
- conditional rendering
- list rendering

## 19. Intermediate interview questions

### What does lifting state up mean?

Moving shared state to the nearest common parent so multiple child components can use the same source of truth.

### What is derived state?

Data calculated from existing state instead of stored separately.

### Why should React state usually be updated immutably?

Because immutable updates are more predictable and help React detect changes correctly.

### When should you use context?

When many components across different levels need the same shared value and prop drilling becomes awkward.

### What is a custom hook used for?

To extract and reuse stateful logic across components.

### Why can overusing `useEffect` be a problem?

Because it can make code harder to reason about, create unnecessary side effects, and hide logic that should be computed directly.

## 20. What to be able to do after this file

After this file, you should be able to:

- structure medium-sized components
- decide where state should live
- fetch and display API data
- build controlled forms
- update arrays and objects safely
- create reusable hooks and components
- understand how a real React app is organized
