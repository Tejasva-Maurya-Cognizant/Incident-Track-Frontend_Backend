# React Basics

This file covers the basic level of React. The goal is to move from "I know JavaScript" to "I understand how React apps are built."

## 1. What React is

React is a JavaScript library for building user interfaces.

React helps you:

- break UI into components
- reuse those components
- manage changing data using state
- automatically update the UI when data changes

Important:

React is not a programming language.

React is not a replacement for JavaScript.

React is a JavaScript library, so you still need JavaScript to use it.

## 2. Why React is useful

Without React, large UIs become hard to manage because:

- DOM updates become manual
- repeated UI logic becomes messy
- state changes are harder to track

React solves this by encouraging:

- component-based design
- declarative UI
- reusable logic patterns

## 3. The core mental model

The most important sentence in React is:

"UI is a function of state."

That means:

- you store data in state
- your component reads that state
- React renders UI based on that state
- when state changes, React updates the UI

This is very different from manually changing the DOM yourself.

## 4. Components

Components are the building blocks of React apps.

A component is usually a JavaScript function that returns JSX.

Example:

```jsx
function Greeting() {
  return <h1>Hello</h1>;
}
```

Use it like:

```jsx
<Greeting />
```

### Why components matter

They let you split a large UI into smaller pieces.

Examples of real component types:

- button
- form
- table row
- navigation bar
- modal
- full page

## 5. JSX

JSX is syntax that looks like HTML but is actually JavaScript syntax used by React.

Example:

```jsx
const element = <h1>Hello, React</h1>;
```

JSX lets you write UI in a readable way.

### Embed JavaScript inside JSX

Use `{}`:

```jsx
const name = "Tejas";
const element = <h1>Hello, {name}</h1>;
```

### JSX rules to remember

- return one parent wrapper
- use `className` instead of `class`
- use `{}` for JavaScript expressions
- components start with uppercase

## 6. Rendering a React app

In React, you mount the app into a root DOM element.

Modern example:

```jsx
import { createRoot } from "react-dom/client";
import App from "./App";

createRoot(document.getElementById("root")).render(<App />);
```

This is how a React app starts in tools like Vite.

## 7. Props

Props are inputs passed from parent component to child component.

Example:

```jsx
function Greeting(props) {
  return <h1>Hello, {props.name}</h1>;
}

function App() {
  return <Greeting name="Tejas" />;
}
```

### Destructured props

More common style:

```jsx
function Greeting({ name }) {
  return <h1>Hello, {name}</h1>;
}
```

### Why props matter

They make components reusable.

The same component can behave differently with different props.

## 8. State

State is data owned by a component that can change over time.

Example using `useState`:

```jsx
import { useState } from "react";

function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>Count: {count}</p>
      <button onClick={() => setCount(count + 1)}>Increase</button>
    </div>
  );
}
```

When `setCount` runs:

- React schedules an update
- the component re-renders
- the new count appears in the UI

## 9. Props vs state

This is a very common interview and beginner question.

### Props

- come from parent
- read-only from the child point of view
- used to configure a component

### State

- owned by the component
- can change over time
- used for dynamic behavior

Simple rule:

- props are input from outside
- state is internal changing data

## 10. Event handling

React handles events using JSX props.

Example:

```jsx
function ButtonExample() {
  const handleClick = () => {
    alert("Clicked");
  };

  return <button onClick={handleClick}>Click me</button>;
}
```

Common React events:

- `onClick`
- `onChange`
- `onSubmit`
- `onKeyDown`
- `onMouseEnter`

## 11. Controlled inputs

In React, forms are often built with controlled inputs.

That means:

- the input value comes from state
- typing updates state

Example:

```jsx
function NameForm() {
  const [name, setName] = useState("");

  return (
    <input
      value={name}
      onChange={(e) => setName(e.target.value)}
      placeholder="Enter your name"
    />
  );
}
```

Why controlled inputs are useful:

- validation is easier
- form data is always in React state
- UI can react to changes instantly

## 12. Conditional rendering

React lets you show different UI based on conditions.

### Using `if`

```jsx
function Status({ isLoggedIn }) {
  if (isLoggedIn) {
    return <p>Welcome back</p>;
  }

  return <p>Please login</p>;
}
```

### Using ternary

```jsx
function Status({ isLoggedIn }) {
  return <p>{isLoggedIn ? "Welcome back" : "Please login"}</p>;
}
```

### Using `&&`

```jsx
function ErrorMessage({ error }) {
  return <div>{error && <p>{error}</p>}</div>;
}
```

These patterns are used constantly in real React apps.

## 13. Lists and keys

React often renders lists by mapping arrays to JSX.

Example:

```jsx
function UserList() {
  const users = [
    { id: 1, name: "A" },
    { id: 2, name: "B" },
  ];

  return (
    <ul>
      {users.map((user) => (
        <li key={user.id}>{user.name}</li>
      ))}
    </ul>
  );
}
```

### Why keys matter

Keys help React identify items in a list.

Good key:

- stable
- unique among siblings

Usually use:

- database ID
- unique identifier

Avoid using array index when stable IDs exist.

## 14. Component composition

React apps are built by putting components inside other components.

Example:

```jsx
function Header() {
  return <h1>My App</h1>;
}

function Content() {
  return <p>Main content</p>;
}

function App() {
  return (
    <div>
      <Header />
      <Content />
    </div>
  );
}
```

This is called composition.

It is one of the main strengths of React.

## 15. Basic styling in React

There are several ways to style React components.

### Normal CSS

```jsx
import "./App.css";
```

### Inline styles

```jsx
<div style={{ color: "red", fontSize: "18px" }}>Hello</div>
```

Important:

Inline styles use JavaScript objects, so property names use camelCase.

### Utility classes

Projects often use systems like Tailwind CSS.

## 16. Basic React app example

This example uses components, props, state, events, and list rendering together.

```jsx
import { useState } from "react";

function TaskItem({ task }) {
  return <li>{task}</li>;
}

export default function App() {
  const [tasks, setTasks] = useState(["Learn JavaScript", "Learn React"]);
  const [input, setInput] = useState("");

  const addTask = () => {
    if (!input.trim()) return;
    setTasks([...tasks, input.trim()]);
    setInput("");
  };

  return (
    <div>
      <h1>Task List</h1>
      <input
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="New task"
      />
      <button onClick={addTask}>Add</button>

      <ul>
        {tasks.map((task, index) => (
          <TaskItem key={index} task={task} />
        ))}
      </ul>
    </div>
  );
}
```

What this teaches:

- `App` owns the state
- input is controlled
- button click updates state
- UI re-renders automatically
- child component receives props

## 17. Common beginner mistakes in React

- Forgetting to import hooks like `useState`
- Calling `setState` directly during render
- Forgetting that state updates cause re-render
- Mutating arrays/objects instead of creating new ones
- Forgetting `key` in list rendering
- Confusing props and state
- Trying to use normal HTML `class` instead of `className`
- Writing JavaScript statements directly in JSX without `{}` and valid expressions

## 18. Basic interview questions

### What is React?

React is a JavaScript library for building user interfaces using reusable components.

### What is JSX?

JSX is syntax that looks like HTML and is used to describe React UI inside JavaScript.

### What is a component?

A component is a reusable piece of UI, usually written as a function that returns JSX.

### What are props?

Props are inputs passed from a parent component to a child component.

### What is state?

State is data managed by a component that can change over time and trigger re-rendering.

### What is the difference between props and state?

Props come from outside and are read-only for the child. State is owned and updated by the component.

### Why do we need keys in lists?

Keys help React identify list items and update them efficiently and correctly.

## 19. What to understand before moving to hooks deeply

Before reading the hooks deep-dive, be comfortable with:

- components
- JSX
- props
- state
- controlled inputs
- event handlers
- conditional rendering
- list rendering

Those are the true basics of React.
