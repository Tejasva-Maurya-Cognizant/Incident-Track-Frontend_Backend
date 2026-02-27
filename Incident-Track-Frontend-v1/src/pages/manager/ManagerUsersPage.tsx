import { useCallback, useEffect, useState } from "react";
import { authApi } from "../../features/auth/api";
import { departmentsApi } from "../../features/departments/api";
import type { UserResponseDto, UserStatus } from "../../features/auth/types";
import type { DepartmentResponseDto } from "../../features/departments/types";
import { getDepartmentId } from "../../features/departments/types";
import type { PageParams } from "../../types/pagination";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";

// ── Status badge ─────────────────────────────────────────────────────────────
function UserStatusBadge({ status }: { status: UserStatus }) {
    const active = status === "ACTIVE";
    return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${active ? "bg-[#DCFCE7] text-[#15803D]" : "bg-[#FEE2E2] text-[#DC2626]"}`}>
            {status}
        </span>
    );
}

// ── Profile drawer ────────────────────────────────────────────────────────────
function ProfileDrawer({
    user,
    deptName,
    onClose,
}: {
    user: UserResponseDto;
    deptName: string;
    onClose: () => void;
}) {
    const rows = [
        { label: "User ID", value: `#${user.userId}` },
        { label: "Username", value: user.username },
        { label: "Email", value: user.email },
        { label: "Role", value: user.role },
        { label: "Department", value: deptName },
        { label: "Status", value: <UserStatusBadge status={user.status} /> },
    ];

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: "rgba(0,0,0,0.4)" }}>
            <div className="card w-full max-w-sm p-5 space-y-4 m-4">
                <div className="flex items-center justify-between">
                    <div>
                        <h3 className="text-sm font-semibold text-slate-900">Employee Profile</h3>
                        <p className="text-xs text-slate-400 mt-0.5">Read-only view</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="h-7 w-7 rounded-lg border flex items-center justify-center text-slate-500 hover:bg-[#FAFCFF]"
                        style={{ borderColor: "var(--border)" }}
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {/* Avatar */}
                <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full bg-[#EEF4FF] flex items-center justify-center text-[#175FFA] font-bold text-lg shrink-0">
                        {user.username.charAt(0).toUpperCase()}
                    </div>
                    <div>
                        <div className="font-semibold text-slate-900 text-sm">{user.username}</div>
                        <div className="text-xs text-slate-400">{user.email}</div>
                    </div>
                </div>

                {/* Info rows */}
                <div className="divide-y" style={{ borderColor: "var(--border)" }}>
                    {rows.map((r) => (
                        <div key={r.label} className="flex items-center justify-between py-2">
                            <span className="text-xs text-slate-400 uppercase tracking-wide font-semibold w-28 shrink-0">{r.label}</span>
                            <span className="text-xs text-slate-800 font-medium text-right">{r.value}</span>
                        </div>
                    ))}
                </div>

                <button
                    className="w-full h-9 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]"
                    style={{ borderColor: "var(--border)" }}
                    onClick={onClose}
                >
                    Close
                </button>
            </div>
        </div>
    );
}

// ── Main page ─────────────────────────────────────────────────────────────────
const DEFAULT_PARAMS: PageParams = { page: 0, size: 10, sortBy: "username", sortDir: "asc" };

export default function ManagerUsersPage() {
    const [users, setUsers] = useState<UserResponseDto[]>([]);
    const [paging, setPaging] = useState({ totalElements: 0, totalPages: 0, page: 0 });
    const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    const [departments, setDepartments] = useState<DepartmentResponseDto[]>([]);
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<UserStatus | "">("");
    const [viewUser, setViewUser] = useState<UserResponseDto | null>(null);

    const load = useCallback(async (p: PageParams) => {
        setLoading(true);
        setErr(null);
        try {
            const res = await authApi.getEmployeesByDepartmentPaged(p);
            setUsers(res.content);
            setPaging({ totalElements: res.totalElements, totalPages: res.totalPages, page: res.page });
        } catch (e: any) {
            setErr(e?.response?.data?.message ?? "Failed to load employees");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        load(params);
    }, [params, load]);

    useEffect(() => {
        departmentsApi.list().then(setDepartments).catch(() => setDepartments([]));
    }, []);

    const handleSort = (field: string) => {
        setParams((prev) => ({
            ...prev,
            page: 0,
            sortBy: field,
            sortDir: prev.sortBy === field && prev.sortDir === "asc" ? "desc" : "asc",
        }));
    };

    const deptName = (id: number) => departments.find((d) => getDepartmentId(d) === id)?.departmentName ?? `#${id}`;

    const filtered = users.filter((u) => {
        if (statusFilter && u.status !== statusFilter) return false;
        if (search && !u.username.toLowerCase().includes(search.toLowerCase()) && !u.email.toLowerCase().includes(search.toLowerCase())) return false;
        return true;
    });

    return (
        <>
            {viewUser && (
                <ProfileDrawer
                    user={viewUser}
                    deptName={deptName(viewUser.departmentId)}
                    onClose={() => setViewUser(null)}
                />
            )}

            <div className="flex flex-col gap-3">
                {/* Header */}
                <div className="flex items-center justify-between gap-2 shrink-0">
                    <div>
                        <h2 className="text-base font-semibold text-slate-900">My Department — Employees</h2>
                        <p className="text-xs text-slate-400 mt-0.5">View employees in your department.</p>
                    </div>
                </div>

                {/* Filters */}
                <div className="card p-3 shrink-0" style={{ position: "sticky", top: 0, zIndex: 10 }}>
                    <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
                        <div>
                            <label className="text-xs text-slate-600">Search</label>
                            <input
                                className="input mt-0.5"
                                placeholder="Username or email…"
                                value={search}
                                onChange={(e) => setSearch(e.target.value)}
                            />
                        </div>
                        <div>
                            <label className="text-xs text-slate-600">Status</label>
                            <select className="input mt-0.5 bg-white" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as any)}>
                                <option value="">All statuses</option>
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                            </select>
                        </div>
                        <div className="flex items-end">
                            <button
                                className="h-9 w-full rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]"
                                style={{ borderColor: "var(--border)" }}
                                onClick={() => { setSearch(""); setStatusFilter(""); }}
                            >
                                Clear
                            </button>
                        </div>
                    </div>
                    {err && <div className="text-xs text-red-600 mt-2">{err}</div>}
                </div>

                {/* Table */}
                <div className="card overflow-x-auto">
                    <table className="w-full text-sm min-w-[560px]">
                        <thead>
                            <tr
                                className="text-xs uppercase tracking-wide text-slate-500 border-b"
                                style={{ position: "sticky", top: 0, zIndex: 5, background: "#F8FAFD", borderColor: "var(--border)" }}
                            >
                                <SortableHeader label="ID" field="userId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-14" />
                                <SortableHeader label="Username" field="username" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-36" />
                                <SortableHeader label="Email" field="email" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} />
                                <th className="text-left px-2 py-2 w-24">Status</th>
                                <th className="text-right px-2 py-2 w-20">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr>
                                    <td className="px-2 py-6 text-slate-400 text-xs" colSpan={5}>Loading…</td>
                                </tr>
                            ) : filtered.length === 0 ? (
                                <tr>
                                    <td className="px-2 py-6 text-slate-400 text-xs" colSpan={5}>No employees found.</td>
                                </tr>
                            ) : (
                                filtered.map((u) => (
                                    <tr key={u.userId} className="border-t hover:bg-[#FAFCFF] transition" style={{ borderColor: "var(--border)" }}>
                                        <td className="px-2 py-2 text-xs font-mono text-slate-400">#{u.userId}</td>
                                        <td className="px-2 py-2 text-xs font-semibold text-slate-900">{u.username}</td>
                                        <td className="px-2 py-2 text-xs text-slate-600">{u.email}</td>
                                        <td className="px-2 py-2"><UserStatusBadge status={u.status} /></td>
                                        <td className="px-2 py-2 text-right">
                                            <button
                                                onClick={() => setViewUser(u)}
                                                className="h-7 px-2.5 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] inline-flex items-center gap-1"
                                                style={{ borderColor: "var(--border)" }}
                                            >
                                                <svg xmlns="http://www.w3.org/2000/svg" className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                                    <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                                </svg>
                                                View
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>

                    {paging.totalPages > 0 && (
                        <Pagination
                            page={paging.page}
                            totalPages={paging.totalPages}
                            totalElements={paging.totalElements}
                            size={params.size}
                            onPageChange={(newPage) => setParams((prev) => ({ ...prev, page: newPage }))}
                            onSizeChange={(newSize) => setParams((prev) => ({ ...prev, size: newSize, page: 0 }))}
                        />
                    )}
                </div>
            </div>
        </>
    );
}
