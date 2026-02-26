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
    <div className="min-h-screen flex items-center justify-center px-4">
      <div className="card w-full max-w-[420px] p-6">
        <h1 className="text-xl font-semibold text-slate-900">IncidentTrack</h1>
        <p className="text-sm text-slate-600 mt-1">Sign in to continue</p>

        <form onSubmit={onSubmit} className="mt-6 space-y-4">
          <div>
            <label className="text-sm text-slate-700">Email</label>
            <input
              className="input mt-1"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm text-slate-700">Password</label>
            <input
              className="input mt-1"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          {err && <div className="text-sm text-red-600">{err}</div>}

          <button className="btn-primary w-full" disabled={loading}>
            {loading ? "Signing in..." : "Sign In"}
          </button>
          <div className="text-sm text-slate-600 text-center mt-4">
            Don&apos;t have an account?{" "}
            <a className="text-[#175FFA] hover:underline" href="/register">
              Create one
            </a>
          </div>
        </form>
      </div>
    </div>
  );
}
