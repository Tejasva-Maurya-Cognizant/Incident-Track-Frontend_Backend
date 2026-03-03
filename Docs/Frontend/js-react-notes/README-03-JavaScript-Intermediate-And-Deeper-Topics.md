# JavaScript Intermediate and Deeper Topics

This file covers the second level of JavaScript understanding. These topics make frontend frameworks like React much easier to understand.

## 1. Scope

Scope decides where a variable can be accessed.

### Global scope

Variables declared outside functions or blocks can often be accessed widely.

### Function scope

Variables declared inside a function are available only inside that function.

### Block scope

Variables declared with `let` and `const` inside `{}` are only available inside that block.

```js
if (true) {
  const message = "inside";
  console.log(message); // works
}

// console.log(message); // error
```

Why scope matters:

- prevents accidental collisions
- controls data visibility
- affects closures

## 2. Hoisting

Hoisting means declarations are processed before code execution in a scope.

### Function declarations

Can be called before their position in code:

```js
sayHi();

function sayHi() {
  console.log("Hi");
}
```

### `var`

Declaration is hoisted, but value starts as `undefined`.

```js
console.log(x); // undefined
var x = 10;
```

### `let` and `const`

They are hoisted too, but cannot be accessed before initialization.

This is often described as the temporal dead zone.

```js
// console.log(y); // error
let y = 10;
```

Practical rule:

- do not rely on hoisting
- declare variables before use

## 3. Execution context

When JavaScript runs code, it creates an execution context.

Very simplified:

- it stores variable definitions
- it tracks function calls
- it manages the current `this`

Important idea:

- JavaScript does not execute everything at once
- it executes in contexts and call stacks

You do not need engine-level details at first, but understanding that function calls create their own context helps explain scope and closures.

## 4. Closures

A closure happens when a function remembers variables from the scope where it was created, even after that outer function has finished.

Example:

```js
function createCounter() {
  let count = 0;

  return function () {
    count++;
    return count;
  };
}

const counter = createCounter();

console.log(counter()); // 1
console.log(counter()); // 2
```

Why this works:

- the inner function closes over `count`
- `count` stays available to it

Why closures matter for React:

- hooks and event handlers often rely on closure behavior
- many "stale state" bugs in React are actually closure misunderstandings

## 5. `this` keyword

`this` refers to an execution context object.

Its value depends on how a function is called.

### In an object method

```js
const user = {
  name: "Tejas",
  greet() {
    console.log(this.name);
  },
};

user.greet(); // Tejas
```

### In regular functions

`this` behavior can be confusing and depends on strict mode and call site.

### In arrow functions

Arrow functions do not create their own `this`.

They use `this` from the surrounding scope.

Practical beginner rule:

- in modern React, you mostly use function components and arrow functions
- but knowing `this` helps understand older JavaScript and class components

## 6. Primitive vs reference values

This is one of the most important JavaScript concepts for React.

### Primitives

Stored/copied by value:

- string
- number
- boolean
- `null`
- `undefined`

```js
let a = 10;
let b = a;
b = 20;

console.log(a); // 10
```

### Objects and arrays

Handled by reference:

```js
const user1 = { name: "A" };
const user2 = user1;

user2.name = "B";

console.log(user1.name); // B
```

Why this matters for React:

- React state updates often depend on creating new objects/arrays
- mutating old objects directly can cause bugs and missed re-renders

## 7. Shallow copy vs deep copy

### Shallow copy

Using spread creates a shallow copy:

```js
const user = { name: "A", address: { city: "X" } };
const copy = { ...user };
```

This copies the top level, but nested objects are still shared.

### Deep copy

Copies nested structures too.

In practice, deep copying is more expensive and should be used carefully.

For many React state updates, shallow copying the updated level is enough if done correctly.

## 8. Array methods in deeper terms

### `map`

- returns a new array
- same length as original
- used for transformation

### `filter`

- returns a new array
- may be shorter
- used for selection

### `find`

- returns the first matching item
- returns `undefined` if none match

### `reduce`

Builds one final value from many items.

```js
const nums = [1, 2, 3, 4];
const total = nums.reduce((sum, n) => sum + n, 0);
console.log(total); // 10
```

Use `reduce` for:

- totals
- grouping
- derived objects

But do not force it where `map` or `filter` is simpler.

## 9. Truthy and falsy values

JavaScript often converts values to boolean in conditions.

Falsy values:

- `false`
- `0`
- `""`
- `null`
- `undefined`
- `NaN`

Everything else is usually truthy.

Example:

```js
if ("hello") {
  console.log("runs");
}
```

Why this matters in React:

Conditional rendering often relies on truthy/falsy checks.

```js
{error && <p>{error}</p>}
```

## 10. Optional chaining and nullish coalescing

### Optional chaining `?.`

Safely access nested properties:

```js
const user = null;
console.log(user?.name); // undefined
```

### Nullish coalescing `??`

Use a fallback only when value is `null` or `undefined`:

```js
const username = user?.name ?? "Guest";
```

These are extremely useful in React when data may not be loaded yet.

## 11. Error handling

Use `try/catch` for code that may fail.

```js
try {
  const data = JSON.parse("invalid json");
} catch (error) {
  console.log("Something went wrong");
}
```

In async functions:

```js
const loadUsers = async () => {
  try {
    const response = await fetch("/api/users");
    const data = await response.json();
    return data;
  } catch (error) {
    console.error("Load failed", error);
  }
};
```

In frontend apps, error handling is critical for:

- API failures
- invalid data
- user feedback

## 12. Promises in deeper terms

A Promise represents a value that will be available later.

States:

- pending
- fulfilled
- rejected

### Promise chain example

```js
fetch("/api/users")
  .then((response) => response.json())
  .then((data) => {
    console.log(data);
  })
  .catch((error) => {
    console.error(error);
  });
```

### Same with `async/await`

```js
try {
  const response = await fetch("/api/users");
  const data = await response.json();
  console.log(data);
} catch (error) {
  console.error(error);
}
```

`async/await` is usually easier to read.

## 13. The event loop (high-level)

JavaScript is single-threaded in the sense that it runs one main piece of code at a time.

So how can it handle timers, network requests, and user events?

Because the runtime uses:

- call stack
- task queues
- event loop

Very high-level flow:

1. synchronous code runs first
2. async operations are handled by browser/runtime APIs
3. callbacks are queued
4. event loop pushes queued callbacks when the call stack is free

Example:

```js
console.log("A");

setTimeout(() => {
  console.log("B");
}, 0);

console.log("C");
```

Output:

```js
A
C
B
```

This concept helps explain async behavior in real apps.

## 14. Classes and prototypes

Modern JavaScript has `class` syntax, but under the hood it is based on prototypes.

### Class example

```js
class User {
  constructor(name) {
    this.name = name;
  }

  greet() {
    return `Hello, ${this.name}`;
  }
}

const user = new User("Tejas");
console.log(user.greet());
```

In modern React:

- functional components are preferred
- class components are less common in new code

Still, understanding classes helps when reading older codebases.

## 15. Immutability and why React cares

Immutability means not changing the original data directly. Instead, create a new version.

Bad:

```js
const user = { name: "A" };
user.name = "B";
```

Better when updating state-like data:

```js
const user = { name: "A" };
const updated = { ...user, name: "B" };
```

Why React cares:

- React often detects changes by reference
- new object/array references make state updates predictable

## 16. DOM, events, and event objects

In browser JavaScript, user actions create event objects.

Example:

```js
document.getElementById("btn").addEventListener("click", (event) => {
  console.log(event.type);
});
```

In React, you still handle events, but through JSX props like `onClick` and `onChange`.

So learning normal DOM events makes React events easier to understand.

## 17. Modules and code organization

Large applications split code into modules.

Good module design means:

- each file has a clear responsibility
- functions are exported and imported where needed
- logic is not repeated everywhere

This is the basis for React app architecture too.

## 18. Deeper implementation examples

### Example 1: Group users by role

```js
const users = [
  { id: 1, role: "ADMIN" },
  { id: 2, role: "EMPLOYEE" },
  { id: 3, role: "EMPLOYEE" },
];

const grouped = users.reduce((acc, user) => {
  if (!acc[user.role]) {
    acc[user.role] = [];
  }
  acc[user.role].push(user);
  return acc;
}, {});

console.log(grouped);
```

### Example 2: Safe nested access

```js
const apiResponse = {
  user: {
    profile: {
      name: "Tejas",
    },
  },
};

const displayName = apiResponse?.user?.profile?.name ?? "Unknown";
console.log(displayName);
```

### Example 3: Immutable array update

```js
const tasks = [
  { id: 1, done: false },
  { id: 2, done: false },
];

const updatedTasks = tasks.map((task) =>
  task.id === 2 ? { ...task, done: true } : task
);

console.log(updatedTasks);
```

This exact pattern is very important for React state updates.

## 19. Intermediate interview questions

### What is a closure?

A closure is when a function remembers variables from the scope where it was created, even after that outer scope has finished.

### What is the difference between primitive and reference types?

Primitive values are copied by value. Objects and arrays are handled by reference.

### Why is immutability useful in frontend apps?

It makes updates more predictable and helps frameworks like React detect state changes correctly.

### What is the event loop?

It is the runtime mechanism that coordinates synchronous code, queued callbacks, and asynchronous tasks.

### What is the difference between `map`, `filter`, and `reduce`?

- `map`: transform items
- `filter`: keep matching items
- `reduce`: combine many items into one final result

### Why is `async/await` commonly used?

Because it makes asynchronous code easier to read and write compared to nested Promise chains.

## 20. What you should now understand before serious React study

Before moving into React, you should be comfortable with:

- scope and closures
- array methods
- objects and immutability
- event handling
- async code
- modules
- optional chaining
- destructuring

These are the concepts React uses constantly, even when the syntax looks different.
