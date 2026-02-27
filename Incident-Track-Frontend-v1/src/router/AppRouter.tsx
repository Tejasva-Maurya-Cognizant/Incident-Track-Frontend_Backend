import React from "react";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { ProtectedRoute } from "./ProtectedRoute";
import { RoleGuard } from "./RoleGuard";

import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import NotFoundPage from "../pages/NotFoundPage";

// import AdminDashboard from "../pages/admin/AdminDashboard";
// import ManagerDashboard from "../pages/manager/ManagerDashboard";
// import EmployeeDashboard from "../pages/employee/EmployeeDashboard";

import AppLayout from "../components/layout/AppLayout";
import HomePage from "../pages/HomePage";

import UnauthorizedPage from "../pages/UnauthorizedPage";

import IncidentsListPage from "../pages/incidents/IncidentsListPage";
import IncidentCreatePage from "../pages/incidents/IncidentCreatePage";
import IncidentDetailPage from "../pages/incidents/IncidentDetailPage";

import AdminDepartmentsPage from "../pages/admin/AdminDepartmentsPage";
import AdminCategoriesPage from "../pages/admin/AdminCategoriesPage";
import AdminUsersPage from "../pages/admin/AdminUsersPage";
import ManagerUsersPage from "../pages/manager/ManagerUsersPage";

import TasksListPage from "../pages/tasks/TasksListPage";
import TaskDetailPage from "../pages/tasks/TaskDetailPage";
import TaskCreatePage from "../pages/tasks/TaskCreatePage";

import NotificationsPage from "../pages/notifications/NotificationsPage";

import AdminAuditLogPage from "../pages/admin/AdminAuditLogPage";
import IncidentBreachesPage from "../pages/compliance/IncidentBreachesPage";

import AdminReportsPage from "../pages/admin/AdminReportsPage";
import ManagerChartsPage from "../pages/manager/ManagerChartsPage";

const router = createBrowserRouter([
  { path: "/login", element: <LoginPage /> },
  { path: "/register", element: <RegisterPage /> },

  // =================================== Previous ================================
  // {
  //   element: <ProtectedRoute />,
  //   children: [
  //     { path: "/", element: <EmployeeDashboard /> }, // default landing (we can redirect based on role later)

  //     {
  //       element: <RoleGuard allow={["ADMIN"]} />,
  //       children: [{ path: "/admin", element: <AdminDashboard /> }],
  //     },
  //     {
  //       element: <RoleGuard allow={["MANAGER"]} />,
  //       children: [{ path: "/manager", element: <ManagerDashboard /> }],
  //     },
  //     {
  //       element: <RoleGuard allow={["EMPLOYEE"]} />,
  //       children: [{ path: "/employee", element: <EmployeeDashboard /> }],
  //     },
  //   ],
  // },

  // ========================== New ===================================
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          { path: "/", element: <HomePage /> },

          {
            element: <RoleGuard allow={["ADMIN"]} />,
            children: [
              { path: "/admin/departments", element: <AdminDepartmentsPage /> },
              { path: "/admin/categories", element: <AdminCategoriesPage /> },
              { path: "/admin/users", element: <AdminUsersPage /> },
              { path: "/admin/audit-logs", element: <AdminAuditLogPage /> },
              { path: "/admin/reports", element: <AdminReportsPage /> },
            ],
          },

          {
            element: <RoleGuard allow={["ADMIN", "MANAGER"]} />,
            children: [
              { path: "/compliance/breaches", element: <IncidentBreachesPage /> },
            ],
          },

          {
            element: <RoleGuard allow={["MANAGER"]} />,
            children: [
              { path: "/manager/users", element: <ManagerUsersPage /> },
              { path: "/manager/charts", element: <ManagerChartsPage /> },
            ],
          },

          { path: "/unauthorized", element: <UnauthorizedPage /> },

          { path: "/incidents", element: <IncidentsListPage /> },
          { path: "/incidents/create", element: <IncidentCreatePage /> },
          { path: "/incidents/:id", element: <IncidentDetailPage /> },

          { path: "/tasks", element: <TasksListPage /> },
          {
            element: <RoleGuard allow={["MANAGER"]} />,
            children: [
              { path: "/tasks/create", element: <TaskCreatePage /> },
            ],
          },
          { path: "/tasks/:id", element: <TaskDetailPage /> },

          { path: "/notifications", element: <NotificationsPage /> },
        ],
      },
    ],
  },

  { path: "*", element: <NotFoundPage /> },
]);

export default function AppRouter() {
  return <RouterProvider router={router} />;
}
