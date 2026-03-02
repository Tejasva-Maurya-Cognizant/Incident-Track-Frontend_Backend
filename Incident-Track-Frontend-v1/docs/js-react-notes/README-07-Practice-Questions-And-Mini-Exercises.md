# Practice Questions and Mini Exercises

This file is for revision, self-testing, and interview preparation.

## 1. JavaScript basic questions

1. What are the primitive data types in JavaScript?
2. What is the difference between `let`, `const`, and `var`?
3. What is the difference between `==` and `===`?
4. What is the difference between an array and an object?
5. What does `map` do?
6. What does `filter` do?
7. What does `find` do?
8. What is destructuring?
9. What does the spread operator do?
10. What is a function parameter and what is a return value?

## 2. JavaScript intermediate questions

1. What is scope?
2. What is hoisting?
3. What is a closure?
4. What is the event loop in simple words?
5. What is the difference between primitive and reference types?
6. Why is immutability important in frontend apps?
7. What is the difference between shallow copy and deep copy?
8. What does `reduce` do?
9. What is optional chaining?
10. What is nullish coalescing?

## 3. React basic questions

1. What is React?
2. What is JSX?
3. What is a component?
4. What are props?
5. What is state?
6. What is the difference between props and state?
7. What is conditional rendering?
8. Why do list items need keys?
9. What is a controlled input?
10. What happens when React state changes?

## 4. React hooks questions

1. What is a hook?
2. What does `useState` return?
3. What does `useEffect` do?
4. What does `useRef` do?
5. What is the difference between `useMemo` and `useCallback`?
6. What is `useContext` used for?
7. What is a custom hook?
8. What are the rules of hooks?
9. Why can missing dependencies in `useEffect` cause bugs?
10. Why does updating a ref not cause a re-render?

## 5. React intermediate questions

1. What does lifting state up mean?
2. What is a single source of truth?
3. What is derived state?
4. When should state stay local?
5. When should state move to a parent?
6. When should you use context?
7. What are loading, error, empty, and success states?
8. Why should arrays and objects in state be updated immutably?
9. Why is component composition important?
10. What does "thinking in components" mean?

## 6. Mini exercises: JavaScript

### Exercise 1: Double every number

Input:

```js
[1, 2, 3, 4]
```

Task:

- return `[2, 4, 6, 8]`

Hint:

- use `map`

### Exercise 2: Filter active users

Input:

```js
[
  { id: 1, active: true },
  { id: 2, active: false },
  { id: 3, active: true }
]
```

Task:

- return only active users

Hint:

- use `filter`

### Exercise 3: Find a user by ID

Task:

- return the user whose `id` is `2`

Hint:

- use `find`

### Exercise 4: Sum all numbers

Input:

```js
[5, 10, 15]
```

Task:

- return `30`

Hint:

- use `reduce`

## 7. Mini exercises: React basics

### Exercise 1: Counter

Build a component with:

- a count shown on screen
- one button to increase count
- one button to decrease count

Topics practiced:

- component
- `useState`
- event handling

### Exercise 2: Controlled input

Build a component with:

- one text input
- one paragraph below it
- the paragraph should show what the user types

Topics practiced:

- controlled input
- `onChange`
- state

### Exercise 3: Conditional welcome

Build a component with:

- one boolean `isLoggedIn`
- show "Welcome back" if true
- show "Please login" if false

Topics practiced:

- conditional rendering

### Exercise 4: Render a list

Build a component that:

- stores an array of names
- uses `.map()` to render them in a list

Topics practiced:

- list rendering
- keys

## 8. Mini exercises: React hooks and intermediate patterns

### Exercise 1: Fetch data

Build a component that:

- loads users from a fake API
- shows loading text while waiting
- shows an error message on failure
- shows the list on success

Topics practiced:

- `useEffect`
- async loading
- loading and error states

### Exercise 2: Search filter

Build a component that:

- stores a list of tasks
- has a search input
- shows only matching tasks

Topics practiced:

- controlled input
- derived state
- `filter`

### Exercise 3: Toggle modal

Build a component that:

- shows a button
- clicking the button opens a simple modal
- clicking close hides the modal

Topics practiced:

- local UI state
- conditional rendering

### Exercise 4: Custom hook

Build a custom hook that:

- tracks window width
- updates when the browser is resized

Topics practiced:

- `useState`
- `useEffect`
- custom hooks
- cleanup

## 9. Practice flow for learning

A good repetition pattern:

1. Read one concept.
2. Type one example.
3. Change the example.
4. Build one mini exercise.
5. Explain it aloud.

This is much better than passive reading only.

## 10. Self-evaluation checklist

You are ready for beginner React project work if you can do these without help:

- write and call simple JavaScript functions
- use `map`, `filter`, and `find`
- read and update objects and arrays
- understand `async/await`
- create a React component
- pass props
- manage local state with `useState`
- handle events
- render lists
- write a basic `useEffect`

You are ready for stronger intermediate work if you can also:

- decide where state should live
- create controlled forms
- update state immutably
- build loading/error/empty flows
- use `useMemo`, `useCallback`, and `useRef` correctly
- create a custom hook
- explain why context is useful

## 11. Suggested next step after these notes

After finishing this note set, the best next step is:

1. build a simple todo app
2. build a users list with API fetch
3. build a small form app
4. read a real React codebase and map the concepts

That is the path from knowledge to actual skill.
