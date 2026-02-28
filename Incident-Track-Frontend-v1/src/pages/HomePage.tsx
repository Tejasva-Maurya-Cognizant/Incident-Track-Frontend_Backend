import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { incidentsApi } from "../features/incidents/api";
import { tasksApi } from "../features/tasks/api";
import type { IncidentResponseDTO } from "../features/incidents/types";
import type { TaskResponseDTO } from "../features/tasks/types";
import StatusBadge from "../components/common/StatusBadge";
import PriorityBadge from "../components/common/PriorityBadge";
import TaskStatusBadge from "../components/common/TaskStatusBadge";

function StatCard({ label, value, accent, loading }: { label: string; value: number | string; accent: string; loading: boolean }) {
  return (
    <div className="card p-4 flex flex-col gap-1">
      <div className="text-xs uppercase tracking-wide text-slate-400">{label}</div>
      <div className={`text-2xl font-bold mt-1 ${loading ? "text-slate-300 animate-pulse" : accent}`}>
        {loading ? "—" : value}
      </div>
    </div>
  );
}

function ActionCard({ href, icon, title, desc }: { href: string; icon: React.ReactNode; title: string; desc: string }) {
  return (
    <Link to={href} className="card p-4 flex items-start gap-3 hover:bg-[#FAFCFF] transition group">
      <div className="w-8 h-8 rounded-lg flex items-center justify-center shrink-0 bg-[#EEF4FF] text-[#175FFA] group-hover:bg-[#dbeafe]">
        {icon}
      </div>
      <div>
        <div className="text-sm font-semibold text-slate-900">{title}</div>
        <div className="text-xs text-slate-400 mt-0.5">{desc}</div>
      </div>
    </Link>
  );
}

const IconPlus = () => <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}><path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" /></svg>;
const IconList = () => <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 10h16M4 14h16M4 18h16" /></svg>;
const IconClipboard = () => <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" /></svg>;
const IconTag = () => <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M7 7h.01M7 3h10a2 2 0 012 2v10a2 2 0 01-.586 1.414l-6 6a2 2 0 01-2.828 0l-6-6A2 2 0 013 15V5a2 2 0 012-2z" /></svg>;
const IconBuilding = () => <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0H3m4-10h.01M7 15h.01M11 11h.01M11 15h.01M15 11h.01M15 15h.01" /></svg>;
const IconUsers = () => <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M17 20h5v-2a4 4 0 00-5-3.87M9 20H4v-2a4 4 0 015-3.87m6-4a4 4 0 11-8 0 4 4 0 018 0zm6 4a2 2 0 100-4 2 2 0 000 4zm-12 0a2 2 0 100-4 2 2 0 000 4z" /></svg>;

export default function HomePage() {
  const { user } = useAuth();
  const role = user?.role ?? "EMPLOYEE";
  const isManager = role === "MANAGER";
  const isAdminOrManager = role === "ADMIN" || role === "MANAGER";

  const [incidents, setIncidents] = useState<IncidentResponseDTO[]>([]);
  const [tasks, setTasks] = useState<TaskResponseDTO[]>([]);
  const [loadingInc, setLoadingInc] = useState(true);
  const [loadingTask, setLoadingTask] = useState(true);

  useEffect(() => {
    const fetchInc = async () => {
      setLoadingInc(true);
      try {
        const res = isAdminOrManager
          ? await incidentsApi.listAllAdminManagerPaged({ page: 0, size: 50, sortBy: "reportedDate", sortDir: "desc" })
          : await incidentsApi.listMinePaged({ page: 0, size: 50, sortBy: "reportedDate", sortDir: "desc" });
        setIncidents(res.content);
      } catch { setIncidents([]); }
      finally { setLoadingInc(false); }
    };
    const fetchTasks = async () => {
      setLoadingTask(true);
      try {
        const res = isAdminOrManager
          ? await tasksApi.listAllPaged({ page: 0, size: 50, sortBy: "createdDate", sortDir: "desc" })
          : await tasksApi.listAssignedToMePaged({ page: 0, size: 50, sortBy: "createdDate", sortDir: "desc" });
        setTasks(res.content);
      } catch { setTasks([]); }
      finally { setLoadingTask(false); }
    };
    fetchInc();
    fetchTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const countOpen = incidents.filter((i) => i.status === "OPEN").length;
  const countInProgress = incidents.filter((i) => i.status === "IN_PROGRESS").length;
  const countResolved = incidents.filter((i) => i.status === "RESOLVED").length;
  const countCritical = incidents.filter((i) => i.isCritical).length;
  const countTaskPending = tasks.filter((t) => t.status === "PENDING").length;
  const countTaskInProgress = tasks.filter((t) => t.status === "IN_PROGRESS").length;
  const countTaskDone = tasks.filter((t) => t.status === "COMPLETED").length;
  const recentIncidents = incidents.slice(0, 5);
  const recentTasks = tasks.slice(0, 5);

  type ActionDef = { href: string; icon: React.ReactNode; title: string; desc: string; roles: string[] };
  const allActions: ActionDef[] = [
    { href: "/incidents/create", icon: <IconPlus />, title: "Create Incident", desc: "Report a new issue.", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { href: "/incidents", icon: <IconList />, title: "All Incidents", desc: "Browse & filter incidents.", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { href: "/tasks", icon: <IconClipboard />, title: "Tasks", desc: "Track assigned tasks.", roles: ["ADMIN", "MANAGER", "EMPLOYEE"] },
    { href: "/tasks/create", icon: <IconPlus />, title: "Create Task", desc: "Assign a task to an employee.", roles: ["MANAGER"] },
    { href: "/admin/categories", icon: <IconTag />, title: "Categories", desc: "Manage incident categories.", roles: ["ADMIN"] },
    { href: "/admin/departments", icon: <IconBuilding />, title: "Departments", desc: "Manage departments.", roles: ["ADMIN"] },
    { href: "/admin/users", icon: <IconUsers />, title: "Users", desc: "Create and manage users.", roles: ["ADMIN"] },
  ].filter((a) => a.roles.includes(role));

  const roleHints: Record<string, string> = {
    ADMIN: "You have full system access.",
    MANAGER: "You can manage incidents and tasks for your department.",
    EMPLOYEE: "You can report incidents and track your tasks.",
  };

  return (
    <div className="space-y-4">

      {/* Greeting */}
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-base font-semibold text-slate-900">Welcome back, {user?.username ?? "—"} </h2>
          <p className="text-xs text-slate-400 mt-0.5">{roleHints[role]}</p>
        </div>
        <span className="text-[10px] px-2 py-1 rounded-full bg-[#EEF4FF] text-[#175FFA] font-semibold shrink-0">{role}</span>
      </div>

      {/* Incident stats */}
      <div>
        <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">
          {isAdminOrManager ? (isManager ? "Department Incidents" : "All Incidents") : "My Incidents"}
        </div>
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          <StatCard label="Open" value={countOpen} accent="text-[#DC2626]" loading={loadingInc} />
          <StatCard label="In Progress" value={countInProgress} accent="text-[#D97706]" loading={loadingInc} />
          <StatCard label="Resolved" value={countResolved} accent="text-[#059669]" loading={loadingInc} />
          <StatCard label="Critical" value={countCritical} accent="text-[#7C3AED]" loading={loadingInc} />
        </div>
      </div>

      {/* Task stats */}
      <div>
        <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide mb-2">
          {isAdminOrManager ? (isManager ? "Department Tasks" : "All Tasks") : "My Tasks"}
        </div>
        <div className="grid grid-cols-3 gap-3">
          <StatCard label="Pending" value={countTaskPending} accent="text-[#D97706]" loading={loadingTask} />
          <StatCard label="In Progress" value={countTaskInProgress} accent="text-[#175FFA]" loading={loadingTask} />
          <StatCard label="Completed" value={countTaskDone} accent="text-[#059669]" loading={loadingTask} />
        </div>
      </div>

      {/* Recent incidents + quick actions */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="card lg:col-span-2">
          <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: "var(--border)" }}>
            <div className="text-sm font-semibold text-slate-900">Recent Incidents</div>
            <Link to="/incidents" className="text-xs text-[#175FFA] hover:underline">View all </Link>
          </div>
          {loadingInc ? (
            <div className="py-8 text-center text-sm text-slate-400">Loading</div>
          ) : recentIncidents.length === 0 ? (
            <div className="py-8 text-center text-sm text-slate-400">No incidents yet.</div>
          ) : (
            <div className="divide-y" style={{ borderColor: "var(--border)" }}>
              {recentIncidents.map((inc) => (
                <div key={inc.incidentId} className="px-4 py-2.5 flex items-center gap-3 hover:bg-[#FAFCFF] transition">
                  <span className="text-xs font-mono text-slate-400 w-10 shrink-0">#{inc.incidentId}</span>
                  <div className="flex-1 min-w-0">
                    <div className="text-xs font-medium text-slate-900 truncate">{inc.categoryName}</div>
                    {inc.subCategory && <div className="text-[10px] text-slate-400 truncate">{inc.subCategory}</div>}
                  </div>
                  <div className="flex items-center gap-1.5 shrink-0">
                    <StatusBadge status={inc.status} />
                    <PriorityBadge severity={inc.calculatedSeverity} />
                  </div>
                  <Link to={`/incidents/${inc.incidentId}`} className="text-[10px] text-[#175FFA] hover:underline shrink-0">View</Link>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex flex-col gap-3">
          <div className="text-xs font-semibold text-slate-500 uppercase tracking-wide">Quick Actions</div>
          {allActions.map((a) => (
            <ActionCard key={a.href + a.title} href={a.href} icon={a.icon} title={a.title} desc={a.desc} />
          ))}
        </div>
      </div>

      {/* Recent Tasks */}
      <div className="card">
        <div className="flex items-center justify-between px-4 py-3 border-b" style={{ borderColor: "var(--border)" }}>
          <div className="text-sm font-semibold text-slate-900">Recent Tasks</div>
          <Link to="/tasks" className="text-xs text-[#175FFA] hover:underline">View all </Link>
        </div>
        {loadingTask ? (
          <div className="py-8 text-center text-sm text-slate-400">Loading</div>
        ) : recentTasks.length === 0 ? (
          <div className="py-8 text-center text-sm text-slate-400">No tasks yet.</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs min-w-[480px]">
              <thead>
                <tr className="border-b text-left text-[10px] uppercase tracking-wide text-slate-400" style={{ borderColor: "var(--border)", background: "#F8FAFD" }}>
                  <th className="px-4 py-2 w-12">ID</th>
                  <th className="px-4 py-2">Title</th>
                  <th className="px-4 py-2 w-28">Status</th>
                  <th className="px-4 py-2 w-20">Incident</th>
                  <th className="px-4 py-2 w-16"></th>
                </tr>
              </thead>
              <tbody className="divide-y" style={{ borderColor: "var(--border)" }}>
                {recentTasks.map((t) => (
                  <tr key={t.taskId} className="hover:bg-[#FAFCFF] transition">
                    <td className="px-4 py-2 font-mono text-slate-400">#{t.taskId}</td>
                    <td className="px-4 py-2 font-medium text-slate-900 max-w-[200px] truncate">{t.title}</td>
                    <td className="px-4 py-2"><TaskStatusBadge status={t.status} /></td>
                    <td className="px-4 py-2 font-mono text-slate-500">
                      <Link to={`/incidents/${t.incidentId}`} className="text-[#175FFA] hover:underline">#{t.incidentId}</Link>
                    </td>
                    <td className="px-4 py-2 text-right">
                      <Link to={`/tasks/${t.taskId}`} className="text-[#175FFA] hover:underline">View</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

    </div>
  );
}
