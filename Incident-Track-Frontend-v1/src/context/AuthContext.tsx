import React, { createContext, useContext, useEffect, useMemo, useState } from "react";
import { TOKEN_STORAGE_KEY } from "../config/constants";
import { authApi } from "../features/auth/api";
import type { AuthRequest, AuthResponse, UserResponseDto, UserRole } from "../features/auth/types";

type AuthState = {
  token: string | null;
  user: UserResponseDto | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  role: UserRole | null;
  login: (req: AuthRequest) => Promise<void>;
  logout: () => void;
  refreshProfile: () => Promise<void>;
};

const AuthContext = createContext<AuthState | null>(null);

export const AuthProvider: React.FC<React.PropsWithChildren> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_STORAGE_KEY));
  const [user, setUser] = useState<UserResponseDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const logout = () => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setToken(null);
    setUser(null);
  };

  const refreshProfile = async () => {
    const profile = await authApi.viewProfile();
    setUser(profile);
  };

  const login = async (req: AuthRequest) => {
    const res: AuthResponse = await authApi.login(req);
    localStorage.setItem(TOKEN_STORAGE_KEY, res.token);
    setToken(res.token);
    // after token saved, fetch profile to get departmentId/status etc
    await refreshProfile();
  };

  useEffect(() => {
    const boot = async () => {
      try {
        if (token) await refreshProfile();
      } catch {
        logout();
      } finally {
        setIsLoading(false);
      }
    };
    boot();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const value = useMemo<AuthState>(
    () => ({
      token,
      user,
      isLoading,
      isAuthenticated: !!token && !!user,
      role: user?.role ?? null,
      login,
      logout,
      refreshProfile,
    }),
    [token, user, isLoading]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
};