# JavaScript Basics

This file covers the basic level of JavaScript. The goal is to make you comfortable reading and writing code.

## 1. What JavaScript is

JavaScript is a programming language used to:

- add logic to websites
- handle user actions like clicks and typing
- update page content
- call APIs
- build full frontend applications

It runs in:

- browsers
- Node.js
- frameworks and tooling environments

For React, JavaScript is the base language. React code is still JavaScript code.

## 2. Values and data types

Everything in JavaScript works with values.

Common primitive types:

- `string`
- `number`
- `boolean`
- `undefined`
- `null`
- `bigint`
- `symbol`

Non-primitive types:

- `object`

Important note:

Arrays and functions are also objects in JavaScript.

### Examples

```js
let name = "Tejas";
let age = 25;
let isLoggedIn = true;
let score = undefined;
let selectedUser = null;
```

### `typeof`

Use `typeof` to check type:

```js
console.log(typeof "hello"); // string
console.log(typeof 10); // number
console.log(typeof true); // boolean
console.log(typeof undefined); // undefined
console.log(typeof {}); // object
console.log(typeof []); // object
console.log(typeof function () {}); // function
```

Important oddity:

```js
console.log(typeof null); // object
```

This is a historical JavaScript quirk.

## 3. Variables: `let`, `const`, `var`

Variables store values.

### `let`

Use when value may change:

```js
let count = 0;
count = count + 1;
```

### `const`

Use when the variable should not be reassigned:

```js
const appName = "Incident Track";
```

Important:

For objects and arrays, `const` means the variable reference cannot be reassigned, but the content can still change.

```js
const user = { name: "A" };
user.name = "B"; // allowed
```

### `var`

Older style. Avoid in modern code unless maintaining legacy code.

Why avoid:

- function-scoped, not block-scoped
- hoisting behavior is more confusing

## 4. Operators

### Arithmetic

```js
let a = 10;
let b = 3;

console.log(a + b); // 13
console.log(a - b); // 7
console.log(a * b); // 30
console.log(a / b); // 3.333...
console.log(a % b); // 1
```

### Comparison

```js
console.log(5 > 3); // true
console.log(5 < 3); // false
console.log(5 >= 5); // true
console.log(5 === 5); // true
console.log(5 !== 4); // true
```

### `==` vs `===`

Use `===` in most cases.

- `==` checks loose equality and may convert types
- `===` checks strict equality

```js
console.log(5 == "5"); // true
console.log(5 === "5"); // false
```

In real projects, strict equality is safer and clearer.

### Logical operators

```js
console.log(true && false); // false
console.log(true || false); // true
console.log(!true); // false
```

These are used a lot in conditions and React rendering.

## 5. Strings

Strings are text values.

```js
const firstName = "Tejas";
const lastName = "V";
```

### Concatenation

```js
const fullName = firstName + " " + lastName;
```

### Template literals

Preferred modern style:

```js
const fullName = `${firstName} ${lastName}`;
console.log(`Hello, ${fullName}`);
```

### Useful string methods

```js
const text = "  React  ";

console.log(text.trim()); // "React"
console.log(text.toUpperCase()); // "  REACT  "
console.log(text.includes("ea")); // true
console.log(text.replace("React", "JavaScript")); // "  JavaScript  "
```

## 6. Conditions

Conditions allow your code to make decisions.

### `if`, `else if`, `else`

```js
const age = 20;

if (age < 18) {
  console.log("Minor");
} else if (age < 60) {
  console.log("Adult");
} else {
  console.log("Senior");
}
```

### Ternary operator

Short form of if/else:

```js
const status = isLoggedIn ? "Welcome" : "Please login";
```

This is used very often in React.

## 7. Loops

Loops repeat work.

### `for`

```js
for (let i = 0; i < 5; i++) {
  console.log(i);
}
```

### `while`

```js
let i = 0;
while (i < 3) {
  console.log(i);
  i++;
}
```

### `for...of`

Great for arrays:

```js
const names = ["A", "B", "C"];

for (const name of names) {
  console.log(name);
}
```

In modern React-heavy code, array methods are used more often than manual loops.

## 8. Functions

Functions group reusable logic.

### Function declaration

```js
function greet(name) {
  return `Hello, ${name}`;
}
```

### Function expression

```js
const greet = function (name) {
  return `Hello, ${name}`;
};
```

### Arrow function

Most common in modern JavaScript and React:

```js
const greet = (name) => {
  return `Hello, ${name}`;
};
```

Short form:

```js
const double = (n) => n * 2;
```

### Parameters and return values

```js
const add = (a, b) => {
  return a + b;
};

const result = add(2, 3); // 5
```

Important rule:

- parameters are inputs
- return value is output

## 9. Arrays

Arrays store ordered collections of values.

```js
const fruits = ["apple", "banana", "mango"];
```

### Access by index

```js
console.log(fruits[0]); // apple
console.log(fruits[1]); // banana
```

### Common array methods

#### `push`

Adds to end:

```js
fruits.push("orange");
```

#### `pop`

Removes last:

```js
fruits.pop();
```

#### `map`

Transforms each item into a new array:

```js
const numbers = [1, 2, 3];
const doubled = numbers.map((n) => n * 2);
console.log(doubled); // [2, 4, 6]
```

This is extremely important in React for rendering lists.

#### `filter`

Keeps only matching items:

```js
const even = numbers.filter((n) => n % 2 === 0);
console.log(even); // [2]
```

#### `find`

Returns the first matching item:

```js
const user = [
  { id: 1, name: "A" },
  { id: 2, name: "B" },
].find((u) => u.id === 2);

console.log(user); // { id: 2, name: "B" }
```

#### `some`

Checks if at least one item matches:

```js
const hasBig = numbers.some((n) => n > 2); // true
```

#### `every`

Checks if all items match:

```js
const allPositive = numbers.every((n) => n > 0); // true
```

## 10. Objects

Objects store key-value data.

```js
const user = {
  name: "Tejas",
  age: 25,
  isAdmin: false,
};
```

### Access properties

```js
console.log(user.name);
console.log(user["age"]);
```

### Update properties

```js
user.age = 26;
user.city = "Chennai";
```

### Why objects matter for React

In React apps, many things are objects:

- props objects
- state objects
- API response objects
- event objects

So reading object syntax comfortably is necessary.

## 11. Arrays of objects

This is one of the most common data shapes in real apps.

```js
const users = [
  { id: 1, name: "A", role: "ADMIN" },
  { id: 2, name: "B", role: "EMPLOYEE" },
  { id: 3, name: "C", role: "MANAGER" },
];
```

Examples:

```js
const names = users.map((u) => u.name);
const managers = users.filter((u) => u.role === "MANAGER");
const admin = users.find((u) => u.role === "ADMIN");
```

This exact shape appears often in frontend development.

## 12. Destructuring

Destructuring extracts values from arrays or objects.

### Object destructuring

```js
const user = { name: "Tejas", age: 25 };
const { name, age } = user;
```

### Array destructuring

```js
const colors = ["red", "green", "blue"];
const [first, second] = colors;
```

Why this matters for React:

- props are often destructured
- hook return values are often array destructured

Example:

```js
const [count, setCount] = useState(0);
```

## 13. Spread and rest syntax

### Spread

Used to copy or expand arrays/objects.

```js
const nums = [1, 2];
const moreNums = [...nums, 3, 4];

const user = { name: "A", age: 20 };
const updatedUser = { ...user, age: 21 };
```

### Rest

Used to collect remaining items.

```js
const [first, ...others] = [10, 20, 30];
console.log(first); // 10
console.log(others); // [20, 30]
```

Function rest parameters:

```js
const sum = (...nums) => nums.reduce((a, b) => a + b, 0);
```

Spread is heavily used in React state updates.

## 14. Basic DOM and events

Before React, JavaScript often directly manipulates the DOM.

Example:

```html
<button id="btn">Click me</button>
<script>
  const btn = document.getElementById("btn");
  btn.addEventListener("click", () => {
    alert("Clicked");
  });
</script>
```

Important beginner understanding:

- the browser gives you elements
- events happen on those elements
- JavaScript reacts to those events

React uses the same idea, but wraps it in a component-based model.

## 15. Basic asynchronous JavaScript

Some code finishes later:

- API requests
- timers
- file reads

### `setTimeout`

```js
setTimeout(() => {
  console.log("Runs later");
}, 1000);
```

### `Promise`

Basic idea:

```js
const promise = new Promise((resolve, reject) => {
  const ok = true;
  if (ok) resolve("Success");
  else reject("Failed");
});
```

### `async/await`

Most readable modern style:

```js
const fetchData = async () => {
  const response = await fetch("/api/users");
  const data = await response.json();
  console.log(data);
};
```

React apps use `async/await` constantly for API calls.

## 16. Modules: `import` and `export`

Modern JavaScript splits code into files.

### Export

```js
export const add = (a, b) => a + b;
```

### Import

```js
import { add } from "./math.js";
```

Default export:

```js
export default function greet() {
  console.log("Hello");
}
```

Import default:

```js
import greet from "./greet.js";
```

React projects use modules everywhere.

## 17. Basic implementation examples

### Example 1: Simple calculator

```js
const add = (a, b) => a + b;
const subtract = (a, b) => a - b;

console.log(add(10, 5));
console.log(subtract(10, 5));
```

### Example 2: Filter active users

```js
const users = [
  { id: 1, name: "A", active: true },
  { id: 2, name: "B", active: false },
  { id: 3, name: "C", active: true },
];

const activeUsers = users.filter((user) => user.active);
console.log(activeUsers);
```

### Example 3: Create labels from data

```js
const tasks = [
  { id: 1, title: "Fix bug" },
  { id: 2, title: "Write docs" },
];

const labels = tasks.map((task) => `#${task.id} ${task.title}`);
console.log(labels);
```

These exact transformations appear constantly in frontend apps.

## 18. Beginner mistakes to avoid

- Using `==` instead of `===`
- Using `var` in modern code
- Forgetting `return` from functions
- Confusing arrays and objects
- Trying to mutate data without understanding references
- Not understanding what `map` returns
- Thinking `find` returns an array (it returns one item or `undefined`)
- Forgetting `await` in async functions

## 19. Basic interview questions

### What is the difference between `let` and `const`?

`let` allows reassignment. `const` does not allow reassignment of the variable reference.

### What is the difference between `==` and `===`?

`==` does loose equality with type coercion. `===` does strict equality without coercion.

### What does `map` do?

It creates a new array by transforming each item in the original array.

### What does `filter` do?

It creates a new array containing only items that match a condition.

### What is an object?

An object is a key-value data structure used to represent related information.

### What is a function?

A function is a reusable block of logic that can take input and return output.

## 20. What to master before moving on

Before going to the deeper JavaScript file, be comfortable with:

- reading arrays of objects
- writing simple functions
- using `map`, `filter`, and `find`
- object property access
- destructuring
- `async/await`
- `import` and `export`

These are the JavaScript foundations that React depends on heavily.
