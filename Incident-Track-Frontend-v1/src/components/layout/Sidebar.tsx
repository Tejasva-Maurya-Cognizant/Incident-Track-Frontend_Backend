import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const linkBase =
  "flex items-center gap-2 rounded-[8px] px-2.5 py-1.5 text-xs transition border";
const inactive =
  "border-transparent text-slate-700 hover:bg-[#FAFCFF] hover:border-[var(--border)]";
const active =
  "bg-white border-[var(--border)] text-slate-900 shadow-sm";

export default function Sidebar() {
  const { user } = useAuth();
  const role = user?.role;

  const links = [
    { to: "/", label: "Home", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { to: "/incidents", label: "Incidents", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { to: "/tasks", label: "Tasks", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { to: "/admin/users", label: "Users", roles: ["ADMIN"] },
    { to: "/manager/users", label: "My Team", roles: ["MANAGER"] },
    { to: "/admin/departments", label: "Departments", roles: ["ADMIN"] },
    { to: "/admin/categories", label: "Categories", roles: ["ADMIN"] },
    { to: "/admin/audit-logs", label: "Audit Logs", roles: ["ADMIN"] },
    { to: "/admin/reports", label: "Reports", roles: ["ADMIN"] },
    { to: "/compliance/breaches", label: "SLA Breaches", roles: ["ADMIN", "MANAGER"] },
    { to: "/manager/charts", label: "Analytics", roles: ["MANAGER"] },
  ].filter((l) => role && l.roles.includes(role));

  return (
    <aside className="card p-3 h-full overflow-y-auto flex flex-col">
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

      <div className="mt-3 space-y-0.5">
        {links.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            className={({ isActive }) =>
              `${linkBase} ${isActive ? active : inactive}`
            }
          >
            <span className="w-1.5 h-1.5 rounded-full bg-slate-300" />
            {l.label}
          </NavLink>
        ))}
      </div>

      <div className="mt-auto border-t pt-3" style={{ borderColor: "var(--border)" }}>
        <div className="text-[10px] text-slate-400">Signed in as</div>
        <div className="text-xs font-medium text-slate-900 truncate">
          {user?.username ?? "—"}
        </div>
        <div className="text-[10px] text-slate-500 truncate">{user?.email ?? ""}</div>
      </div>
    </aside>
  );
}