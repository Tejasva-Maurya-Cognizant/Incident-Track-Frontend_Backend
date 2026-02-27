import { useCallback, useEffect, useState } from "react";
import { authApi } from "../../features/auth/api";
import { departmentsApi } from "../../features/departments/api";
import type { UserResponseDto, UserRole, UserStatus } from "../../features/auth/types";
import type { DepartmentResponseDto } from "../../features/departments/types";
import { getDepartmentId } from "../../features/departments/types";
import type { PageParams } from "../../types/pagination";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";

// ── Role badge ────────────────────────────────────────────────────────────────
function RoleBadge({ role }: { role: UserRole }) {
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

// ── Edit modal ────────────────────────────────────────────────────────────────
interface EditModalProps {
    user: UserResponseDto;
    departments: DepartmentResponseDto[];
    onClose: () => void;
    onSaved: () => void;
}

function EditModal({ user, departments, onClose, onSaved }: EditModalProps) {
    const [username, setUsername] = useState(user.username);
    const [email, setEmail] = useState(user.email);
    const [role, setRole] = useState<UserRole>(user.role);
    const [departmentId, setDepartmentId] = useState<number>(user.departmentId);
    const [password, setPassword] = useState("");
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    const onSave = async () => {
        setSaving(true);
        setErr(null);
        try {
            const body: Record<string, unknown> = { username, email, role, departmentId };
            if (password.trim()) body.password = password.trim();
            await authApi.updateUserDetailsByAdmin(user.userId, body as any);
            onSaved();
            onClose();
        } catch (e: any) {
            setErr(e?.response?.data?.message ?? "Update failed");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: "rgba(0,0,0,0.4)" }}>
            <div className="card w-full max-w-md p-5 space-y-4 m-4">
                <div className="flex items-center justify-between">
                    <div>
                        <h3 className="text-sm font-semibold text-slate-900">Edit User</h3>
                        <p className="text-xs text-slate-400 mt-0.5">#{user.userId} · {user.email}</p>
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

                <div className="space-y-3">
                    <div>
                        <label className="text-xs text-slate-600 font-medium">Username</label>
                        <input className="input mt-0.5" value={username} onChange={(e) => setUsername(e.target.value)} />
                    </div>
                    <div>
                        <label className="text-xs text-slate-600 font-medium">Email</label>
                        <input className="input mt-0.5" type="email" value={email} onChange={(e) => setEmail(e.target.value)} />
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                        <div>
                            <label className="text-xs text-slate-600 font-medium">Role</label>
                            <select className="input mt-0.5 bg-white" value={role} onChange={(e) => setRole(e.target.value as UserRole)}>
                                <option value="EMPLOYEE">EMPLOYEE</option>
                                <option value="MANAGER">MANAGER</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>
                        </div>
                        <div>
                            <label className="text-xs text-slate-600 font-medium">Department</label>
                            <select className="input mt-0.5 bg-white" value={departmentId} onChange={(e) => setDepartmentId(Number(e.target.value))}>
                                {departments.map((d) => (
                                    <option key={getDepartmentId(d)} value={getDepartmentId(d)}>
                                        {d.departmentName}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                    <div>
                        <label className="text-xs text-slate-600 font-medium">New Password <span className="text-slate-400 font-normal">(leave blank to keep current)</span></label>
                        <input
                            className="input mt-0.5"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                        />
                    </div>
                </div>

                {err && <div className="text-xs text-red-600">{err}</div>}

                <div className="flex gap-2 pt-1">
                    <button
                        className="btn-primary flex-1"
                        onClick={onSave}
                        disabled={saving}
                    >
                        {saving ? "Saving…" : "Save Changes"}
                    </button>
                    <button
                        className="flex-1 h-9 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]"
                        style={{ borderColor: "var(--border)" }}
                        onClick={onClose}
                    >
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
}

// ── Create User modal ─────────────────────────────────────────────────────────
interface CreateModalProps {
    departments: DepartmentResponseDto[];
    onClose: () => void;
    onCreated: () => void;
}

function CreateModal({ departments, onClose, onCreated }: CreateModalProps) {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [role, setRole] = useState<UserRole>("EMPLOYEE");
    const [departmentId, setDepartmentId] = useState<number>(getDepartmentId(departments[0]) ?? 0);
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    const onCreate = async () => {
        if (!username.trim() || !email.trim() || !password.trim()) {
            setErr("Username, email, and password are required.");
            return;
        }
        setSaving(true);
        setErr(null);
        try {
            await authApi.register({ username: username.trim(), email: email.trim(), password: password.trim(), role, departmentId, status: "ACTIVE" });
            onCreated();
            onClose();
        } catch (e: any) {
            setErr(e?.response?.data ?? e?.response?.data?.message ?? "Create failed");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ background: "rgba(0,0,0,0.4)" }}>
            <div className="card w-full max-w-md p-5 space-y-4 m-4">
                <div className="flex items-center justify-between">
                    <div>
                        <h3 className="text-sm font-semibold text-slate-900">Create New User</h3>
                        <p className="text-xs text-slate-400 mt-0.5">Add a user to the system</p>
                    </div>
                    <button onClick={onClose} className="h-7 w-7 rounded-lg border flex items-center justify-center text-slate-500 hover:bg-[#FAFCFF]" style={{ borderColor: "var(--border)" }}>
                        <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                <div className="space-y-3">
                    <div>
                        <label className="text-xs text-slate-600 font-medium">Username</label>
                        <input className="input mt-0.5" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="johndoe" />
                    </div>
                    <div>
                        <label className="text-xs text-slate-600 font-medium">Email</label>
                        <input className="input mt-0.5" type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="john@example.com" />
                    </div>
                    <div>
                        <label className="text-xs text-slate-600 font-medium">Password</label>
                        <input className="input mt-0.5" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
                    </div>
                    <div className="grid grid-cols-2 gap-2">
                        <div>
                            <label className="text-xs text-slate-600 font-medium">Role</label>
                            <select className="input mt-0.5 bg-white" value={role} onChange={(e) => setRole(e.target.value as UserRole)}>
                                <option value="EMPLOYEE">EMPLOYEE</option>
                                <option value="MANAGER">MANAGER</option>
                                <option value="ADMIN">ADMIN</option>
                            </select>
                        </div>
                        <div>
                            <label className="text-xs text-slate-600 font-medium">Department</label>
                            <select className="input mt-0.5 bg-white" value={departmentId} onChange={(e) => setDepartmentId(Number(e.target.value))}>
                                {departments.map((d) => (
                                    <option key={getDepartmentId(d)} value={getDepartmentId(d)}>
                                        {d.departmentName}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                </div>

                {err && <div className="text-xs text-red-600">{err}</div>}

                <div className="flex gap-2 pt-1">
                    <button className="btn-primary flex-1" onClick={onCreate} disabled={saving}>
                        {saving ? "Creating…" : "Create User"}
                    </button>
                    <button className="flex-1 h-9 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF]" style={{ borderColor: "var(--border)" }} onClick={onClose}>
                        Cancel
                    </button>
                </div>
            </div>
        </div>
    );
}

// ── Main page ─────────────────────────────────────────────────────────────────
const DEFAULT_PARAMS: PageParams = { page: 0, size: 10, sortBy: "username", sortDir: "asc" };

export default function AdminUsersPage() {
    const [users, setUsers] = useState<UserResponseDto[]>([]);
    const [paging, setPaging] = useState({ totalElements: 0, totalPages: 0, page: 0 });
    const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    const [departments, setDepartments] = useState<DepartmentResponseDto[]>([]);

    // Filters
    const [roleFilter, setRoleFilter] = useState<UserRole | "">("");
    const [statusFilter, setStatusFilter] = useState<UserStatus | "">("");
    const [search, setSearch] = useState("");

    // Modals
    const [editUser, setEditUser] = useState<UserResponseDto | null>(null);
    const [showCreate, setShowCreate] = useState(false);

    // Status toggle feedback
    const [togglingId, setTogglingId] = useState<number | null>(null);

    const load = useCallback(async (p: PageParams) => {
        setLoading(true);
        setErr(null);
        try {
            const res = await authApi.getAllUsersPaged(p);
            setUsers(res.content);
            setPaging({ totalElements: res.totalElements, totalPages: res.totalPages, page: res.page });
        } catch (e: any) {
            setErr(e?.response?.data?.message ?? "Failed to load users");
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

    const handleToggleStatus = async (u: UserResponseDto) => {
        const action = u.status === "ACTIVE" ? "Deactivate" : "Activate";
        const detail = u.status === "ACTIVE" ? "They will not be able to log in." : "They will regain login access.";
        if (!confirm(`${action} user "${u.username}"? ${detail}`)) return;
        setTogglingId(u.userId);
        try {
            await authApi.toggleUserStatus(u.userId);
        } catch (e: any) {
            alert(e?.response?.data?.message ?? "Status toggle failed");
        } finally {
            setTogglingId(null);
            load(params);
        }
    };

    // Client-side filter on loaded page
    const filtered = users.filter((u) => {
        if (roleFilter && u.role !== roleFilter) return false;
        if (statusFilter && u.status !== statusFilter) return false;
        if (search && !u.username.toLowerCase().includes(search.toLowerCase()) && !u.email.toLowerCase().includes(search.toLowerCase())) return false;
        return true;
    });

    const deptName = (id: number) => departments.find((d) => getDepartmentId(d) === id)?.departmentName ?? `#${id}`;

    return (
        <>
            {editUser && (
                <EditModal
                    user={editUser}
                    departments={departments}
                    onClose={() => setEditUser(null)}
                    onSaved={() => load(params)}
                />
            )}
            {showCreate && (
                <CreateModal
                    departments={departments}
                    onClose={() => setShowCreate(false)}
                    onCreated={() => load(params)}
                />
            )}

            <div className="flex flex-col h-full gap-3">
                {/* Header */}
                <div className="flex items-center justify-between gap-2 shrink-0">
                    <div>
                        <h2 className="text-base font-semibold text-slate-900">User Management</h2>
                        <p className="text-xs text-slate-400 mt-0.5">View, edit, and manage all system users.</p>
                    </div>
                    <button
                        onClick={() => setShowCreate(true)}
                        className="btn-primary inline-flex items-center gap-1.5 shrink-0"
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                        </svg>
                        Add User
                    </button>
                </div>

                {/* Toolbar */}
                <div className="card p-2 flex items-center gap-2 flex-nowrap shrink-0">
                    <div className="relative flex-[2] min-w-0">
                        <svg className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400 pointer-events-none" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35M16.65 16.65A7.5 7.5 0 1 0 4.5 4.5a7.5 7.5 0 0 0 12.15 12.15z" />
                        </svg>
                        <input
                            className="input pl-8 h-8 text-xs w-full"
                            placeholder="Search username or email…"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                        />
                    </div>
                    <select className="input h-8 text-xs bg-white flex-1 min-w-0" value={roleFilter} onChange={(e) => setRoleFilter(e.target.value as any)}>
                        <option value="">All Roles</option>
                        <option value="ADMIN">ADMIN</option>
                        <option value="MANAGER">MANAGER</option>
                        <option value="EMPLOYEE">EMPLOYEE</option>
                    </select>
                    <select className="input h-8 text-xs bg-white flex-1 min-w-0" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value as any)}>
                        <option value="">All Statuses</option>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                    </select>
                    <span className="shrink-0 text-xs text-slate-400 whitespace-nowrap">
                        {filtered.length} result{filtered.length !== 1 ? "s" : ""}
                    </span>
                    {(search || roleFilter || statusFilter) && (
                        <button
                            className="shrink-0 h-8 px-3 rounded-[8px] bg-white border text-xs text-slate-600 hover:bg-[#FAFCFF] whitespace-nowrap"
                            style={{ borderColor: "var(--border)" }}
                            onClick={() => { setSearch(""); setRoleFilter(""); setStatusFilter(""); }}
                        >
                            Clear filters
                        </button>
                    )}
                </div>
                {err && <div className="text-xs text-red-600 shrink-0">{err}</div>}

                {/* Table card */}
                <div className="card flex flex-col flex-1 overflow-hidden">
                    <div className="overflow-x-auto flex-1">
                        <table className="w-full text-sm min-w-[700px]">
                            <thead>
                                <tr
                                    className="text-xs uppercase tracking-wide text-slate-500 border-b"
                                    style={{ position: "sticky", top: 0, zIndex: 5, background: "#F8FAFD", borderColor: "var(--border)" }}
                                >
                                    <SortableHeader label="ID" field="userId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-14" />
                                    <SortableHeader label="Username" field="username" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-36" />
                                    <SortableHeader label="Email" field="email" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-52" />
                                    <th className="text-left px-2 py-2 w-24">Role</th>
                                    <SortableHeader label="Department" field="departmentId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-32" />
                                    <th className="text-left px-2 py-2 w-24">Status</th>
                                    <th className="text-right px-2 py-2 w-36">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr>
                                        <td className="px-2 py-6 text-slate-400 text-xs" colSpan={7}>Loading…</td>
                                    </tr>
                                ) : filtered.length === 0 ? (
                                    <tr>
                                        <td className="px-2 py-6 text-slate-400 text-xs" colSpan={7}>No users found.</td>
                                    </tr>
                                ) : (
                                    filtered.map((u) => (
                                        <tr key={u.userId} className="border-t hover:bg-[#FAFCFF] transition" style={{ borderColor: "var(--border)" }}>
                                            <td className="px-2 py-2 text-xs font-mono text-slate-400">#{u.userId}</td>
                                            <td className="px-2 py-2 text-xs font-semibold text-slate-900">{u.username}</td>
                                            <td className="px-2 py-2 text-xs text-slate-600 max-w-[200px] truncate">{u.email}</td>
                                            <td className="px-2 py-2"><RoleBadge role={u.role} /></td>
                                            <td className="px-2 py-2 text-xs text-slate-600">{deptName(u.departmentId)}</td>
                                            <td className="px-2 py-2">
                                                {/* Status dropdown — toggles ACTIVE ↔ INACTIVE */}
                                                <select
                                                    className="text-[10px] font-semibold rounded-full px-2 py-0.5 border-0 outline-none"
                                                    style={{
                                                        background: u.status === "ACTIVE" ? "#DCFCE7" : "#FEE2E2",
                                                        color: u.status === "ACTIVE" ? "#15803D" : "#DC2626",
                                                        cursor: "pointer",
                                                    }}
                                                    value={u.status}
                                                    disabled={togglingId === u.userId}
                                                    onChange={() => handleToggleStatus(u)}
                                                >
                                                    <option value="ACTIVE">ACTIVE</option>
                                                    <option value="INACTIVE">INACTIVE</option>
                                                </select>
                                            </td>
                                            <td className="px-2 py-2 text-right">
                                                <div className="inline-flex gap-1">
                                                    <button
                                                        onClick={() => setEditUser(u)}
                                                        className="h-7 px-2.5 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] inline-flex items-center gap-1"
                                                        style={{ borderColor: "var(--border)" }}
                                                    >
                                                        <svg xmlns="http://www.w3.org/2000/svg" className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                                            <path strokeLinecap="round" strokeLinejoin="round" d="M15.232 5.232l3.536 3.536M9 11l6-6 3 3-6 6H9v-3z" />
                                                        </svg>
                                                        Edit
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
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
