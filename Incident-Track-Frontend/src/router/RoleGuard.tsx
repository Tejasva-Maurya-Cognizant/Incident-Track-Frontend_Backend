import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import type { UserRole } from "../features/auth/types";
import { useAuth } from "../context/AuthContext";

export const RoleGuard: React.FC<{ allow: UserRole[] }> = ({ allow }) => {
  const { isLoading, role } = useAuth();
  if (isLoading) return null;
  if (!role) return <Navigate to="/login" replace />;
  return allow.includes(role) ? <Outlet /> : <Navigate to="/unauthorized" replace />;
};