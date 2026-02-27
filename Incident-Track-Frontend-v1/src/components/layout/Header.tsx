import { useMemo } from "react";
import { useAuth } from "../../context/AuthContext";

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return "Good morning";
  if (h < 18) return "Good afternoon";
  return "Good evening";
}

export default function Header({ onMenuClick }: { onMenuClick?: () => void }) {
  const { user } = useAuth();

  const greeting = useMemo(() => getGreeting(), []);
  const role = user?.role ?? "—";

  return (
    <header className="card px-3 py-2.5 sm:px-4 sm:py-3">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-2.5">
          {/* Mobile hamburger */}
          <button
            className="lg:hidden h-8 w-8 rounded-lg border flex items-center justify-center text-slate-600 hover:bg-[#FAFCFF] shrink-0"
            style={{ borderColor: "var(--border)" }}
            onClick={onMenuClick}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <div>
            <div className="text-xs text-slate-500">{greeting},</div>
            <div className="text-base font-semibold text-slate-900 leading-tight">
              Hi, {user?.username ?? "there"} 👋{" "}
              <span className="text-xs font-medium text-slate-500">({role})</span>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

