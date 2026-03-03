import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { tasksApi } from "../../features/tasks/api";
import { authApi } from "../../features/auth/api";
import type { TaskResponseDTO, TaskStatus } from "../../features/tasks/types";
import type { UserResponseDto } from "../../features/auth/types";
import type { PageParams } from "../../types/pagination";
import { useAuth } from "../../context/AuthContext";
import TaskStatusBadge from "../../components/common/TaskStatusBadge";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";
import {
  TableBodyRow,
  TableHeaderCell,
  TableIdCell,
  TABLE_HEADER_ROW_CLASS,
  TABLE_HEADER_ROW_STYLE,
} from "../../components/common/TablePrimitives";

const DEFAULT_PARAMS: PageParams = {
  page: 0,
  size: 10,
  sortBy: "createdDate",
  sortDir: "desc",
};

const TASK_STATUSES: TaskStatus[] = ["PENDING", "IN_PROGRESS", "COMPLETED"];

export default function TasksListPage() {
  const { user } = useAuth();
  const role = user?.role ?? "EMPLOYEE";
  const isEmployee = role === "EMPLOYEE";
  const isManager = role === "MANAGER";
  const navigate = useNavigate();

  const [statusFilter, setStatusFilter] = useState<TaskStatus | "">("");
  const [search, setSearch] = useState("");
  const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);
  const [items, setItems] = useState<TaskResponseDTO[]>([]);
  const [paging, setPaging] = useState({ totalElements: 0, totalPages: 0, page: 0 });
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);
  // Map of userId → user info for name display in Assigned To column
  const [userMap, setUserMap] = useState<Record<number, UserResponseDto>>({});

  const load = useCallback(
    async (p: PageParams) => {
      setLoading(true);
      setErr(null);
      try {
        let res;
        if (isEmployee) {
          // Employee sees only tasks assigned to them
          res = await tasksApi.listAssignedToMePaged(p);
        } else if (statusFilter) {
          // Admin / Manager filtered by status (manager is department-scoped by backend)
          res = await tasksApi.listByStatusPaged(statusFilter, p);
        } else {
          // Admin sees all tasks; Manager sees department tasks via backend scoping
          res = await tasksApi.listAllPaged(p);
        }
        setItems(res.content);
        setPaging({ totalElements: res.totalElements, totalPages: res.totalPages, page: res.page });

        // Resolve unique assignedTo user IDs to names
        if (!isEmployee) {
          const ids = [...new Set(res.content.map((t) => t.assignedTo))];
          const results = await Promise.allSettled(ids.map((id) => authApi.getUserById(id)));
          const map: Record<number, UserResponseDto> = {};
          results.forEach((r, i) => { if (r.status === "fulfilled") map[ids[i]] = r.value; });
          setUserMap((prev) => ({ ...prev, ...map }));
        }
      } catch (e: any) {
        setErr(e?.response?.data?.message ?? "Failed to load tasks");
      } finally {
        setLoading(false);
      }
    },
    [statusFilter, isEmployee, isManager]
  );

  useEffect(() => {
    load(params);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params, load]);

  const handleSort = (field: string) => {
    const newDir =
      params.sortBy === field && params.sortDir === "asc" ? "desc" : "asc";
    setParams((p) => ({ ...p, sortBy: field, sortDir: newDir, page: 0 }));
  };

  const handleStatusFilter = (s: TaskStatus | "") => {
    setStatusFilter(s);
    setSearch("");
    setParams({ ...DEFAULT_PARAMS });
  };

  const filteredItems = items.filter((task) => {
    if (!search) return true;
    const q = search.toLowerCase();
    return (
      String(task.taskId).includes(q) ||
      task.title.toLowerCase().includes(q) ||
      String(task.incidentId).includes(q) ||
      (userMap[task.assignedTo]?.username ?? "").toLowerCase().includes(q)
    );
  });

  const fmtDate = (iso: string | null) => {
    if (!iso) return "—";
    const d = new Date(iso);
    return (
      <>
        <span className="block">{d.toLocaleDateString()}</span>
        <span className="block text-slate-400">{d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</span>
      </>
    );
  };

  return (
    <div className="flex flex-col gap-3 h-full">
      {/* ── Header ── */}
      <div className="flex items-center justify-between gap-2 shrink-0">
        <div>
          <h1 className="text-base font-semibold text-slate-900">Tasks</h1>
          <p className="text-xs text-slate-400 mt-0.5">
            {isEmployee
              ? "Tasks assigned to you"
              : isManager
                ? "Tasks for incidents in your department"
                : "All tasks in the system"}
          </p>
        </div>
        {isManager && (
          <Link to="/tasks/create" className="btn-primary flex items-center gap-1.5 shrink-0">
            <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            Create Task
          </Link>
        )}
      </div>

      {/* Toolbar */}
      <div className="card p-2 flex items-center gap-2 flex-nowrap shrink-0">
        {/* Search */}
        <div className="relative flex-[2] min-w-0">
          <svg
            className="absolute left-2 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400 pointer-events-none"
            xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}
          >
            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z" />
          </svg>
          <input
            className="input pl-8 h-8 text-xs w-full"
            placeholder="Search by task ID, title, incident ID, assignee…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        {!isEmployee && (
          <select
            className="input h-8 text-xs bg-white flex-1 min-w-0"
            value={statusFilter}
            onChange={(e) => handleStatusFilter(e.target.value as TaskStatus | "")}
          >
            <option value="">All Statuses</option>
            {TASK_STATUSES.map((s) => (
              <option key={s} value={s}>{s === "IN_PROGRESS" ? "In Progress" : s.charAt(0) + s.slice(1).toLowerCase()}</option>
            ))}
          </select>
        )}
        <span className="shrink-0 text-xs text-slate-400 whitespace-nowrap">
          {filteredItems.length} result{filteredItems.length !== 1 ? "s" : ""}
        </span>
        {(search || statusFilter) && (
          <button
            className="shrink-0 h-8 px-3 rounded-[8px] bg-white border text-xs text-slate-600 hover:bg-[#FAFCFF] whitespace-nowrap"
            style={{ borderColor: "var(--border)" }}
            onClick={() => { setSearch(""); handleStatusFilter(""); }}
          >
            Clear filters
          </button>
        )}
      </div>

      {/* Table card */}
      <div className="card flex flex-col flex-1 overflow-hidden">
        {loading ? (
          <div className="py-10 text-center text-sm text-slate-500">Loading tasks…</div>
        ) : err ? (
          <div className="py-8 text-center text-sm text-red-600">{err}</div>
        ) : filteredItems.length === 0 ? (
          <div className="py-10 text-center">
            <div className="text-3xl mb-2">📋</div>
            <div className="text-sm font-medium text-slate-700">{search ? "No tasks match your search" : "No tasks found"}</div>
            <p className="text-xs text-slate-400 mt-1">
              {search ? "Try a different keyword." : isManager ? "Create a task to get started." : "No tasks have been assigned to you yet."}
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto flex-1">
            <table className="w-full text-sm min-w-[580px]">
              <thead>
                <tr className={TABLE_HEADER_ROW_CLASS} style={TABLE_HEADER_ROW_STYLE}>
                  <SortableHeader label="Task ID" field="taskId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-2 py-2 w-12" />
                  <SortableHeader label="Title" field="title" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-2 py-2 w-[240px]" />
                  <TableHeaderCell className="w-[90px]">Status</TableHeaderCell>
                  <TableHeaderCell className="w-[80px]">Incident</TableHeaderCell>
                  {!isEmployee && (
                    <TableHeaderCell className="w-[110px]">Assigned To</TableHeaderCell>
                  )}
                  <SortableHeader label="Created" field="createdDate" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-2 py-2 w-[100px]" />
                  <SortableHeader label="Due" field="dueDate" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-2 py-2 w-[100px]" />
                  <TableHeaderCell className="w-[90px]">Action</TableHeaderCell>
                </tr>
              </thead>
              <tbody className="divide-y" style={{ borderColor: "var(--border)" }}>
                {filteredItems.map((task, index) => (
                  <TableBodyRow
                    key={task.taskId}
                    index={index}
                    onClick={() => navigate(`/tasks/${task.taskId}`)}
                  >
                    <TableIdCell id={task.taskId} />
                    <td className="px-2 py-2">
                      <span className="font-medium text-slate-900 line-clamp-2 max-w-[240px] block leading-snug text-xs">
                        {task.title}
                      </span>
                    </td>
                    <td className="px-2 py-2">
                      <TaskStatusBadge status={task.status} />
                    </td>
                    <td className="px-2 py-2 text-slate-600 font-mono text-xs">
                      <Link
                        to={`/incidents/${task.incidentId}?fromTask=true`}
                        className="text-[#175FFA] hover:underline"
                        onClick={(e) => e.stopPropagation()}
                      >
                        #{task.incidentId}
                      </Link>
                    </td>
                    {!isEmployee && (
                      <td className="px-2 py-2 text-slate-700 text-xs">
                        {userMap[task.assignedTo] ? (
                          <span className="flex flex-col gap-0">
                            <span className="font-medium text-slate-900 text-xs">{userMap[task.assignedTo].username}</span>
                            <span className="text-slate-400 font-mono text-[10px]">#{task.assignedTo}</span>
                          </span>
                        ) : (
                          <span className="text-slate-400 font-mono">#{task.assignedTo}</span>
                        )}
                      </td>
                    )}
                    <td className="px-2 py-2 text-xs text-slate-600 leading-snug">{fmtDate(task.createdDate)}</td>
                    <td className="px-2 py-2 text-xs text-slate-600 leading-snug">{fmtDate(task.dueDate)}</td>
                    <td className="px-2 py-2">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/tasks/${task.taskId}`);
                        }}
                        className="h-7 px-3 text-xs rounded-md border font-medium hover:bg-[#FAFCFF] transition-colors"
                        style={{ borderColor: "var(--border)" }}
                      >
                        View
                      </button>
                    </td>
                  </TableBodyRow>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {!loading && !err && paging.totalPages > 0 && (
          <Pagination
            page={paging.page}
            totalPages={paging.totalPages}
            totalElements={paging.totalElements}
            size={params.size}
            onPageChange={(p) => setParams((prev) => ({ ...prev, page: p }))}
            onSizeChange={(s) => setParams({ ...DEFAULT_PARAMS, size: s })}
          />
        )}
      </div>
    </div>
  );
}
