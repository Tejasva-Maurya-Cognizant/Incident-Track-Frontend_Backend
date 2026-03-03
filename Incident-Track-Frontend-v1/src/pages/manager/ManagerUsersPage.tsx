import { useCallback, useEffect, useState } from "react";
import { authApi } from "../../features/auth/api";
import { departmentsApi } from "../../features/departments/api";
import type { UserResponseDto, UserRole, UserStatus } from "../../features/auth/types";
import type { DepartmentResponseDto } from "../../features/departments/types";
import { getDepartmentId } from "../../features/departments/types";
import type { PageParams } from "../../types/pagination";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";
import {
    TableBodyRow,
    TableHeaderCell,
    TableIdCell,
    TABLE_HEADER_ROW_CLASS,
} from "../../components/common/TablePrimitives";

// ── Status badge ─────────────────────────────────────────────────────────────
function UserStatusBadge({ status }: { status: UserStatus }) {
    const active = status === "ACTIVE";

    return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${active ? "bg-[#DCFCE7] text-[#15803D]" : "bg-[#FEE2E2] text-[#DC2626]"}`}>
            {status}
        </span>
    );
}

function UserRoleBadge({ role }: { role: UserRole }) {
    const map: Record<UserRole, string> = {
        ADMIN: "bg-[#EEF4FF] text-[#175FFA]",
        MANAGER: "bg-[#FEF9C3] text-[#A16207]",
        EMPLOYEE: "bg-[#F0FDF4] text-[#15803D]",
    };

    return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${map[role] ?? "bg-slate-100 text-slate-600"}`}>
            {role}
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
                    {rows.map((row) => (
                        <div key={row.label} className="flex items-center justify-between py-2">
                            <span className="text-xs text-slate-400 uppercase tracking-wide font-semibold w-28 shrink-0">{row.label}</span>
                            <span className="text-xs text-slate-800 font-medium text-right">{row.value}</span>
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

    const load = useCallback(async (pageParams: PageParams) => {
        setLoading(true);
        setErr(null);
        try {
            const res = await authApi.getEmployeesByDepartmentPaged(pageParams);
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

    const filtered = users.filter((user) => {
        if (statusFilter && user.status !== statusFilter) return false;
        if (
            search &&
            !user.username.toLowerCase().includes(search.toLowerCase()) &&
            !user.email.toLowerCase().includes(search.toLowerCase())
        ) {
            return false;
        }
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

            <div className="flex flex-col h-full gap-3">
                <div className="flex items-center justify-between gap-2 shrink-0">
                    <div>
                        <h2 className="text-base font-semibold text-slate-900">My Department - Employees</h2>
                        <p className="text-xs text-slate-400 mt-0.5">View employees in your department.</p>
                    </div>
                </div>

                <div className="card p-2 flex items-center gap-2 flex-nowrap shrink-0">
                    <div className="relative flex-[2] min-w-0">
                        <svg className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400 pointer-events-none" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35M16.65 16.65A7.5 7.5 0 1 0 4.5 4.5a7.5 7.5 0 0 0 12.15 12.15z" />
                        </svg>
                        <input
                            className="input pl-8 h-8 text-xs w-full"
                            placeholder="Search username or email..."
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                    <select className="input h-8 text-xs bg-white flex-1 min-w-0" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as UserStatus | "")}>
                        <option value="">All Statuses</option>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                    </select>
                    <span className="shrink-0 text-xs text-slate-400 whitespace-nowrap">
                        {filtered.length} result{filtered.length !== 1 ? "s" : ""}
                    </span>
                    {(search || statusFilter) && (
                        <button
                            className="shrink-0 h-8 px-3 rounded-[8px] bg-white border text-xs text-slate-600 hover:bg-[#FAFCFF] whitespace-nowrap"
                            style={{ borderColor: "var(--border)" }}
                            onClick={() => {
                                setSearch("");
                                setStatusFilter("");
                            }}
                        >
                            Clear filters
                        </button>
                    )}
                </div>
                {err && <div className="text-xs text-red-600 shrink-0">{err}</div>}

                <div className="card flex flex-col flex-1 overflow-hidden">
                    <div className="overflow-auto flex-1">
                        <table className="w-full text-sm min-w-[660px]">
                            <thead>
                                <tr className={TABLE_HEADER_ROW_CLASS} style={{ borderColor: "var(--border)" }}>
                                    <SortableHeader label="Employee ID" field="userId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-14 sticky top-0 z-[5] bg-[#F8FAFD]" />
                                    <SortableHeader label="Username" field="username" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-36 sticky top-0 z-[5] bg-[#F8FAFD]" />
                                    <SortableHeader label="Email" field="email" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="sticky top-0 z-[5] bg-[#F8FAFD]" />
                                    <TableHeaderCell className="w-24 sticky top-0 z-[5] bg-[#F8FAFD]">Role</TableHeaderCell>
                                    <TableHeaderCell className="w-24 sticky top-0 z-[5] bg-[#F8FAFD]">Status</TableHeaderCell>
                                    <TableHeaderCell className="w-20 sticky top-0 z-[5] bg-[#F8FAFD]">Actions</TableHeaderCell>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr>
                                        <td className="px-2 py-6 text-slate-400 text-xs" colSpan={6}>Loading...</td>
                                    </tr>
                                ) : filtered.length === 0 ? (
                                    <tr>
                                        <td className="px-2 py-6 text-slate-400 text-xs" colSpan={6}>No employees found.</td>
                                    </tr>
                                ) : (
                                    filtered.map((user, index) => (
                                        <TableBodyRow
                                            key={user.userId}
                                            index={index}
                                        >
                                            <TableIdCell id={user.userId} />
                                            <td className="px-2 py-2 text-xs font-semibold text-slate-900">{user.username}</td>
                                            <td className="px-2 py-2 text-xs text-slate-600 max-w-[220px] truncate">{user.email}</td>
                                            <td className="px-2 py-2"><UserRoleBadge role={user.role} /></td>
                                            <td className="px-2 py-2"><UserStatusBadge status={user.status} /></td>
                                            <td className="px-2 py-2">
                                                <div className="inline-flex gap-1">
                                                    <button
                                                        onClick={() => setViewUser(user)}
                                                        className="h-7 px-2.5 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] inline-flex items-center gap-1"
                                                        style={{ borderColor: "var(--border)" }}
                                                    >
                                                        <svg xmlns="http://www.w3.org/2000/svg" className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                                            <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                                            <path strokeLinecap="round" strokeLinejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.477 0 8.268 2.943 9.542 7-1.274 4.057-5.065 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                                        </svg>
                                                        View
                                                    </button>
                                                </div>
                                            </td>
                                        </TableBodyRow>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>

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
