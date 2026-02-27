import React, { useState } from "react";
import type { UserRole, UserStatus } from "../features/auth/types";
import { authApi } from "../features/auth/api";

import { useEffect } from "react";
import { departmentsApi } from "../features/departments/api";
import type { DepartmentResponseDto } from "../features/departments/types";
import { getDepartmentId } from "../features/departments/types";

export default function RegisterPage() {
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [departmentId, setDepartmentId] = useState<number>(1);
  const [role, setRole] = useState<UserRole>("EMPLOYEE");

  // If your backend requires exact enum values, set the correct default here.
  const [status, setStatus] = useState<UserStatus>("ACTIVE");

  const [err, setErr] = useState<string | null>(null);
  const [ok, setOk] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [departments, setDepartments] = useState<DepartmentResponseDto[]>([]);
  const [deptLoading, setDeptLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const list = await departmentsApi.list();
        setDepartments(list);
        // default to first department if available
        if (list.length > 0) setDepartmentId(getDepartmentId(list[0]));
      } finally {
        setDeptLoading(false);
      }
    };
    load();
  }, []);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    setOk(null);
    setLoading(true);

    try {
      await authApi.register({
        username,
        email,
        password,
        departmentId,
        role,
        status,
      });

      setOk("Registered successfully. You can login now.");
      // optional redirect:
      setTimeout(() => (window.location.href = "/login"), 600);
    } catch (error: any) {
      setErr(error?.response?.data?.message ?? "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-8" style={{ background: "var(--bg)" }}>
      <div className="card w-full max-w-[520px] p-7 sm:p-8">

        {/* Brand mark */}
        <div className="flex items-center gap-2.5 mb-6">
          <div className="w-9 h-9 rounded-[10px] flex items-center justify-center shrink-0" style={{ background: "var(--brand)" }}>
            <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
            </svg>
          </div>
          <span className="text-base font-semibold text-slate-900 tracking-tight">IncidentTrack</span>
        </div>

        <h2 className="text-lg font-semibold text-slate-900">Create your account</h2>
        <p className="text-sm text-slate-400 mt-1 mb-6">Fill in the details below to get started</p>

        <form onSubmit={onSubmit} className="space-y-4">
          {/* Username + Email */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-slate-600 mb-1.5">Username</label>
              <input
                className="input h-10 text-sm"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. john_doe"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-600 mb-1.5">Email</label>
              <input
                className="input h-10 text-sm"
                type="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@company.com"
              />
            </div>
          </div>

          {/* Password */}
          <div>
            <label className="block text-sm font-medium text-slate-600 mb-1.5">Password</label>
            <input
              className="input h-10 text-sm"
              type="password"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          {/* Department + Role */}
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-slate-600 mb-1.5">Department</label>
              <select
                className="input h-10 text-sm bg-white"
                value={departmentId}
                disabled={deptLoading || departments.length === 0}
                onChange={(e) => setDepartmentId(Number(e.target.value))}
              >
                {deptLoading
                  ? <option>Loading…</option>
                  : departments.map((d) => (
                    <option key={getDepartmentId(d)} value={getDepartmentId(d)}>{d.departmentName}</option>
                  ))
                }
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-600 mb-1.5">Role</label>
              <select
                className="input h-10 text-sm bg-white"
                value={role}
                onChange={(e) => setRole(e.target.value as UserRole)}
              >
                <option value="EMPLOYEE">Employee</option>
                <option value="MANAGER">Manager</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
          </div>

          {/* Status */}
          <div>
            <label className="block text-sm font-medium text-slate-600 mb-1.5">Status</label>
            <select
              className="input h-10 text-sm bg-white"
              value={status}
              onChange={(e) => setStatus(e.target.value as UserStatus)}
            >
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
            </select>
          </div>

          {err && (
            <div className="flex items-center gap-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-[8px] px-3 py-1.5">
              {err}
            </div>
          )}
          {ok && (
            <div className="flex items-center gap-2 text-xs text-green-700 bg-green-50 border border-green-200 rounded-[8px] px-3 py-1.5">
              {ok}
            </div>
          )}

          <button className="btn-primary w-full h-10 text-sm mt-1" disabled={loading}>
            {loading ? "Creating account…" : "Create account"}
          </button>
        </form>

        <p className="text-sm text-slate-400 text-center mt-5">
          Already have an account?{" "}
          <a className="text-[#175FFA] hover:underline font-medium" href="/login">Sign in</a>
        </p>
      </div>
    </div>
  );
}
