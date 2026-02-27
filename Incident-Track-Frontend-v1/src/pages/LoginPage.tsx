import React, { useState } from "react";
import { useAuth } from "../context/AuthContext";

export default function LoginPage() {
  const { login } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    setLoading(true);
    try {
      await login({ email, password });
      window.location.href = "/";
    } catch (error: any) {
      setErr(error?.response?.data?.message ?? "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4" style={{ background: "var(--bg)" }}>
      <div className="card w-full max-w-[440px] p-7 sm:p-8">

        {/* Brand mark */}
        <div className="flex items-center gap-2.5 mb-6">
          <div className="w-9 h-9 rounded-[10px] flex items-center justify-center shrink-0" style={{ background: "var(--brand)" }}>
            <svg xmlns="http://www.w3.org/2000/svg" className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v4m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
            </svg>
          </div>
          <span className="text-base font-semibold text-slate-900 tracking-tight">IncidentTrack</span>
        </div>

        <h2 className="text-lg font-semibold text-slate-900">Sign in to your account</h2>
        <p className="text-sm text-slate-400 mt-1 mb-6">Enter your credentials to continue</p>

        <form onSubmit={onSubmit} className="space-y-4">
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
          <div>
            <label className="block text-sm font-medium text-slate-600 mb-1.5">Password</label>
            <input
              className="input h-10 text-sm"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
            />
          </div>

          {err && (
            <div className="flex items-center gap-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-[8px] px-3 py-1.5">
              {err}
            </div>
          )}

          <button className="btn-primary w-full h-10 text-sm mt-1" disabled={loading}>
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>

        <p className="text-sm text-slate-400 text-center mt-5">
          Don&apos;t have an account?{" "}
          <a className="text-[#175FFA] hover:underline font-medium" href="/register">Create one</a>
        </p>
      </div>
    </div>
  );
}
