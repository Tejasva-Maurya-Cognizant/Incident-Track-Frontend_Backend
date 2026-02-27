import { useState } from "react";
import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useNotifications } from "../../context/NotificationContext";
import ProfilePanel from "../common/ProfilePanel";

const linkBase =
  "flex items-center gap-2 rounded-[8px] px-2.5 py-1.5 text-xs transition border";
const inactive =
  "border-transparent text-slate-700 hover:bg-[#FAFCFF] hover:border-[var(--border)]";
const active =
  "bg-white border-[var(--border)] text-slate-900 shadow-sm";

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

export default function Sidebar() {
  const { user, logout } = useAuth();
  const { unreadCount } = useNotifications();
  const role = user?.role;
  const [profileOpen, setProfileOpen] = useState(false);

  const allLinks = [
    { to: "/", label: "Home", section: "general", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { to: "/incidents", label: "Incidents", section: "general", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { to: "/tasks", label: "Tasks", section: "general", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { to: "/notifications", label: "Notifications", section: "general", roles: ["ADMIN", "MANAGER", "EMPLOYEE"], badge: true as const },
    { to: "/admin/users", label: "Users", section: "management", roles: ["ADMIN"] },
    { to: "/manager/users", label: "My Team", section: "management", roles: ["MANAGER"] },
    { to: "/admin/departments", label: "Departments", section: "management", roles: ["ADMIN"] },
    { to: "/admin/categories", label: "Categories", section: "management", roles: ["ADMIN"] },
    { to: "/admin/audit-logs", label: "Audit Logs", section: "insights", roles: ["ADMIN"] },
    { to: "/admin/reports", label: "Reports", section: "insights", roles: ["ADMIN"] },
    { to: "/compliance/breaches", label: "SLA Breaches", section: "insights", roles: ["ADMIN", "MANAGER"] },
    { to: "/manager/charts", label: "Analytics", section: "insights", roles: ["MANAGER"] },
  ].filter((l) => role && l.roles.includes(role));

  const sections: { key: string; label: string }[] = [
    { key: "general", label: "General" },
    { key: "management", label: "Management" },
    { key: "insights", label: "Insights" },
  ];

  return (
    <aside className="card p-3 h-full overflow-y-auto flex flex-col">
      {/* Logo / workspace header */}
      <div className="flex items-center justify-between">
        <div>
          <div className="text-[10px] text-slate-400 uppercase tracking-wide">Workspace</div>
          <div className="text-sm font-semibold text-slate-900">IncidentTrack</div>
        </div>
        {role && (
          <span className="text-[10px] px-1.5 py-0.5 rounded-full bg-[#DCEBFF] text-[#1D4ED8]">
            {role}
          </span>
        )}
      </div>

      <hr className="mt-3" style={{ borderColor: "var(--border)" }} />

      {/* Sectioned nav */}
      <nav className="mt-3 flex flex-col gap-3">
        {sections.map((sec) => {
          const secLinks = allLinks.filter((l) => l.section === sec.key);
          if (secLinks.length === 0) return null;
          return (
            <div key={sec.key}>
              <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wider px-1 mb-1">
                {sec.label}
              </div>
              <div className="space-y-0.5">
                {secLinks.map((l) => (
                  <NavLink
                    key={l.to}
                    to={l.to}
                    className={({ isActive }) =>
                      `${linkBase} ${isActive ? active : inactive}`
                    }
                  >
                    <span className="w-1.5 h-1.5 rounded-full bg-slate-300 shrink-0" />
                    <span className="flex-1">{l.label}</span>
                    {l.badge && unreadCount > 0 && (
                      <span className="ml-auto min-w-[18px] h-[18px] px-1 rounded-full bg-[#175FFA] text-white text-[9px] font-bold flex items-center justify-center leading-none">
                        {unreadCount > 99 ? "99+" : unreadCount}
                      </span>
                    )}
                  </NavLink>
                ))}
              </div>
            </div>
          );
        })}
      </nav>

      <div className="mt-auto border-t pt-2.5 flex flex-col gap-1.5" style={{ borderColor: "var(--border)" }}>
        {/* Profile button */}
        <button
          onClick={() => setProfileOpen(true)}
          className="flex items-center gap-2 w-full rounded-[8px] px-2 py-1.5 border text-left hover:bg-[#FAFCFF] transition-colors"
          style={{ borderColor: "var(--border)" }}
        >
          <UserAvatar name={user?.username ?? "?"} />
          <div className="flex flex-col min-w-0">
            <span className="text-xs font-medium text-slate-800 truncate leading-tight">{user?.username ?? "—"}</span>
            <span className="text-[10px] text-slate-400 truncate">{user?.email ?? ""}</span>
          </div>
        </button>

        {/* Logout button */}
        <button
          onClick={() => { logout(); window.location.href = "/login"; }}
          className="flex items-center gap-2 w-full rounded-[8px] px-2.5 py-1.5 border border-transparent text-xs font-medium text-slate-600 hover:bg-[#FAFCFF] hover:border-[var(--border)] transition-colors"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          Logout
        </button>
      </div>

      <ProfilePanel open={profileOpen} onClose={() => setProfileOpen(false)} />
    </aside>
  );
}