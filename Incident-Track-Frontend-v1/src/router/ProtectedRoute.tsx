import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export const ProtectedRoute: React.FC = () => {
  const { isLoading, isAuthenticated } = useAuth();
  if (isLoading) return null; // later replace with spinner
  return isAuthenticated ? <Outlet /> : <Navigate to="/login" replace />;
};