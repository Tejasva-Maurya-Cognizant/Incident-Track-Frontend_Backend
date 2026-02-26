import React, { useState } from "react";
import type { UserRole, UserStatus } from "../features/auth/types";
import { authApi } from "../features/auth/api";

import { useEffect } from "react";
import { departmentsApi } from "../features/departments/api";
import type { DepartmentResponseDto } from "../features/departments/types";
import { getDepartmentId } from "../features/departments/types";
import Select from "../components/common/Select";

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
    <div className="min-h-screen flex items-center justify-center bg-[var(--bg)] px-4">
      <div className="card w-full max-w-[520px] p-6">
        <h1 className="text-xl font-semibold text-slate-900">Create account</h1>
        <p className="text-sm text-slate-600 mt-1">
          Register to start using IncidentTrack
        </p>

        <form onSubmit={onSubmit} className="mt-6 space-y-4">
          <div>
            <label className="text-sm text-slate-700">Username</label>
            <input
              className="input mt-1"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="e.g. it_admin"
            />
          </div>

          <div>
            <label className="text-sm text-slate-700">Email</label>
            <input
              className="input mt-1"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. it_admin@company.com"
            />
          </div>

          <div>
            <label className="text-sm text-slate-700">Password</label>
            <input
              className="input mt-1"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              {/* <label className="text-sm text-slate-700">Department ID</label> */}
              <Select
                label="Department"
                value={departmentId}
                disabled={deptLoading || departments.length === 0}
                onChange={(v) => setDepartmentId(Number(v))}
                placeholder={deptLoading ? "Loading..." : "Select department"}
                options={departments.map((d) => ({
                  value: getDepartmentId(d),
                  label: d.departmentName,
                }))}
              />
              <p className="text-xs text-slate-500 mt-1">
                {/* Departments loaded from <code>/api/departments</code> */}
              </p>
            </div>

            <div>
              <label className="text-sm text-slate-700">Role</label>
              <select
                className="input mt-1"
                value={role}
                onChange={(e) => setRole(e.target.value as UserRole)}
              >
                <option value="EMPLOYEE">EMPLOYEE</option>
                <option value="MANAGER">MANAGER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
          </div>

          <div>
            <label className="text-sm text-slate-700">Status</label>
            <input
              className="input mt-1"
              value={String(status)}
              onChange={(e) => setStatus(e.target.value as UserStatus)}
              placeholder="e.g. ACTIVE"
            />
            <p className="text-xs text-slate-500 mt-1">
              Set to your backend enum value (e.g. ACTIVE).
            </p>
          </div>

          {err && <div className="text-sm text-red-600">{err}</div>}
          {ok && <div className="text-sm text-emerald-700">{ok}</div>}

          <button className="btn-primary w-full" disabled={loading}>
            {loading ? "Creating..." : "Create account"}
          </button>

          <div className="text-sm text-slate-600 text-center">
            Already have an account?{" "}
            <a className="text-[#175FFA] hover:underline" href="/login">
              Sign in
            </a>
          </div>
        </form>
      </div>
    </div>
  );
}
