import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App.tsx";
import { attachInterceptors } from "./lib/axios/interceptors.ts";
import { AuthProvider } from "./context/AuthContext.tsx";
import { NotificationProvider } from "./context/NotificationContext.tsx";

attachInterceptors(() => {
  // on 401
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
