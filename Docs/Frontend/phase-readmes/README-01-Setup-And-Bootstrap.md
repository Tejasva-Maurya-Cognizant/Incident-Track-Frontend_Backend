# Frontend Phase 1: Setup, Tooling, and Project Bootstrap

This file explains how the frontend starts before any business feature is built.

## 1. What this frontend is

This is a React + TypeScript frontend created with Vite. It is the UI for an incident management system where users can:

- log in and register
- create and track incidents
- create and track tasks
- receive real-time notifications
- manage users, departments, categories, audit logs, reports, and compliance views

The active frontend folder is `Incident-Track-Frontend-v1`.

## 2. Core technologies used

From `package.json`, the main stack is:

- `react` and `react-dom`
- `typescript`
- `vite`
- `react-router-dom`
- `axios`
- `tailwindcss`
- `recharts`
- `eslint`

## 3. Why Vite is used here

Vite handles the developer workflow:

- `npm run dev` starts the development server
- it supports modern ES module development
- it builds optimized production bundles
- it integrates cleanly with React and TypeScript

This project follows the typical Vite flow:

- `index.html` contains the `#root` node
- `src/main.tsx` mounts the app
- `src/App.tsx` passes control to the router

## 4. How to install and run

Open a terminal in `Incident-Track-Frontend-v1`, then run:

```bash
npm install
npm run dev
```

Other scripts:

```bash
npm run build
npm run lint
npm run preview
```

## 5. Backend dependency

The frontend expects:

```ts
export const API_BASE_URL = "http://localhost:8888/api";
```

So the backend should be running before API-driven pages can work.

## 6. Key bootstrap files

- `index.html`: browser entry document
- `src/main.tsx`: actual React entry point
- `src/App.tsx`: minimal app wrapper
- `src/index.css`: Tailwind import and global design tokens
- `vite.config.ts`: Vite configuration
- `tsconfig*.json`: TypeScript configuration
- `eslint.config.js`: linting rules

## 7. How the app actually boots

`src/main.tsx` performs the startup sequence:

1. imports global CSS
2. attaches Axios interceptors
3. mounts `AuthProvider`
4. mounts `NotificationProvider`
5. renders `App` inside `StrictMode`

Core pattern:

```tsx
attachInterceptors(() => {
  localStorage.removeItem("it_token");
  window.location.href = "/login";
});

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AuthProvider>
      <NotificationProvider>
        <App />
      </NotificationProvider>
    </AuthProvider>
  </StrictMode>,
);
```

Why this matters:

- every request gets centralized auth behavior
- auth state exists before protected screens render
- notifications can depend on the logged-in user

## 8. Role of `App.tsx`

`App.tsx` is intentionally small:

```tsx
export default function App() {
  return <AppRouter />;
}
```

That is a common clean architecture choice: keep startup simple, move real logic into router, contexts, and pages.

## 9. What to understand before later phases

Before learning features, understand these base ideas:

- Vite is the build and dev tool
- React renders the UI
- TypeScript defines data shape
- Axios handles backend calls
- Router controls screen selection
- Providers expose shared state

If you understand this file, you understand how the project starts from zero and becomes a working React application.
