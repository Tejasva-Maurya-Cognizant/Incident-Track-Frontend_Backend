import { useMemo, useState } from "react";
import { useAuth } from "../../context/AuthContext";
import ProfilePanel from "../common/ProfilePanel";

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return "Good morning";
  if (h < 18) return "Good afternoon";
  return "Good evening";
}

function UserAvatar({ name }: { name: string }) {
  const initials = name
    .split(" ")
    .map((w) => w[0]?.toUpperCase() ?? "")
    .slice(0, 2)
    .join("");
  return (
    <div
      className="h-7 w-7 rounded-full flex items-center justify-center text-white text-xs font-semibold select-none shrink-0"
      style={{ background: "var(--brand)" }}
    >
      {initials || "?"}
    </div>
  );
}

export default function Header({ onMenuClick }: { onMenuClick?: () => void }) {
  const { user, logout } = useAuth();
  const [profileOpen, setProfileOpen] = useState(false);

  const greeting = useMemo(() => getGreeting(), []);
  const role = user?.role ?? "—";

  return (
    <>
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

          <div className="flex items-center gap-2">
            {/* Avatar / Profile button */}
            <button
              onClick={() => setProfileOpen(true)}
              title="View profile"
              className="flex items-center gap-2 h-9 pl-2 pr-3 rounded-[8px] bg-white border text-sm font-medium text-slate-800 hover:bg-[#FAFCFF] transition-colors"
              style={{ borderColor: "var(--border)" }}
            >
              <UserAvatar name={user?.username ?? "?"} />
              <div className="hidden sm:flex flex-col items-start leading-tight">
                <span className="text-xs font-medium text-slate-800 max-w-[100px] truncate">
                  {user?.username ?? "Profile"}
                </span>
                <span className="text-[10px] text-slate-400">{role}</span>
              </div>
              {/* chevron */}
              <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5 text-slate-400 ml-1" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M19 9l-7 7-7-7" />
              </svg>
            </button>

            {/* Logout */}
            <button
              onClick={() => {
                logout();
                window.location.href = "/login";
              }}
              title="Logout"
              className="h-9 px-3 rounded-[8px] bg-white border text-sm font-medium text-slate-800 hover:bg-[#FAFCFF] flex items-center gap-1.5 transition-colors"
              style={{ borderColor: "var(--border)" }}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              <span className="hidden sm:inline">Logout</span>
            </button>
          </div>
        </div>
      </header>

      <ProfilePanel open={profileOpen} onClose={() => setProfileOpen(false)} />
    </>
  );
}

