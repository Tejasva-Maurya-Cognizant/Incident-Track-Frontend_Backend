import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { tasksApi } from "../../features/tasks/api";
import { authApi } from "../../features/auth/api";
import type { TaskResponseDTO, TaskStatus } from "../../features/tasks/types";
import type { UserResponseDto } from "../../features/auth/types";
import { useAuth } from "../../context/AuthContext";
import TaskStatusBadge from "../../components/common/TaskStatusBadge";

const NEXT_STATUSES: Record<string, TaskStatus[]> = {
    EMPLOYEE: ["IN_PROGRESS", "COMPLETED"],
    MANAGER: ["PENDING", "IN_PROGRESS", "COMPLETED"],
    ADMIN: ["PENDING", "IN_PROGRESS", "COMPLETED"],
};

function InfoRow({ label, children }: { label: string; children: React.ReactNode }) {
    return (
        <div
            className="flex items-start justify-between py-2.5 border-b last:border-0"
            style={{ borderColor: "var(--border)" }}
        >
            <span className="text-xs font-semibold text-slate-400 uppercase tracking-wide w-32 shrink-0">
                {label}
            </span>
            <span className="text-sm text-slate-800 font-medium text-right">{children}</span>
        </div>
    );
}

function UserChip({ userId, userMap }: { userId: number; userMap: Record<number, UserResponseDto> }) {
    const u = userMap[userId];
    if (!u) return <span className="font-mono text-slate-400">#{userId}</span>;
    return (
        <span className="inline-flex flex-col items-end gap-0.5">
            <span className="font-medium text-slate-900">{u.username}</span>
            <span className="text-xs text-slate-400 font-mono">#{u.userId}</span>
        </span>
    );
}

export default function TaskDetailPage() {
    const { id } = useParams();
    const taskId = Number(id);
    const navigate = useNavigate();
    const { user } = useAuth();
    const role = user?.role ?? "EMPLOYEE";

    const canUpdateStatus = role === "EMPLOYEE" || role === "MANAGER";
    const isAdminOrManager = role === "ADMIN" || role === "MANAGER";

    const [data, setData] = useState<TaskResponseDTO | null>(null);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);
    const [userMap, setUserMap] = useState<Record<number, UserResponseDto>>({});

    const [newStatus, setNewStatus] = useState<TaskStatus>("IN_PROGRESS");
    const [updating, setUpdating] = useState(false);
    const [updateErr, setUpdateErr] = useState<string | null>(null);
    const [updateOk, setUpdateOk] = useState(false);

    const loadUserInfo = async (assignedTo: number, assignedBy: number) => {
        const ids = [...new Set([assignedTo, assignedBy])];
        const results = await Promise.allSettled(ids.map((uid) => authApi.getUserById(uid)));
        const map: Record<number, UserResponseDto> = {};
        results.forEach((r, i) => {
            if (r.status === "fulfilled") map[ids[i]] = r.value;
        });
        setUserMap(map);
    };

    const load = async () => {
        setLoading(true);
        setErr(null);
        try {
            let res: TaskResponseDTO;
            if (isAdminOrManager) {
                res = await tasksApi.getById(taskId);
            } else {
                const page = await tasksApi.listAssignedToMePaged({
                    page: 0, size: 100, sortBy: "createdDate", sortDir: "desc",
                });
                const found = page.content.find((t) => t.taskId === taskId);
                if (!found) {
                    setErr("Task not found or you don't have access.");
                    setLoading(false);
                    return;
                }
                res = found;
            }
            setData(res);
            setNewStatus(res.status);
            loadUserInfo(res.assignedTo, res.assignedBy);
        } catch (e: any) {
            setErr(e?.response?.data?.message ?? "Failed to load task");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!taskId) return;
        load();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [taskId]);

    const onUpdateStatus = async () => {
        setUpdateErr(null);
        setUpdateOk(false);
        setUpdating(true);
        try {
            await tasksApi.updateStatus(taskId, { status: newStatus });
            setUpdateOk(true);
            await load();
        } catch (e: any) {
            setUpdateErr(e?.response?.data?.message ?? "Status update failed");
        } finally {
            setUpdating(false);
        }
    };

    const fmtDate = (iso: string | null) => {
        if (!iso) return "—";
        return new Date(iso).toLocaleString();
    };

    if (loading)
        return (
            <div className="flex items-center justify-center py-24 text-sm text-slate-500">
                Loading task...
            </div>
        );

    if (err)
        return (
            <div className="card p-8 text-center text-sm text-red-600">
                <div className="text-2xl mb-2">warning</div>
                {err}
                <div className="mt-4">
                    <button onClick={() => navigate("/tasks")} className="text-[#175FFA] hover:underline text-sm">
                        Back to Tasks
                    </button>
                </div>
            </div>
        );

    if (!data) return null;

    const allowedStatuses = NEXT_STATUSES[role] ?? [];

    return (
        <div className="space-y-5 max-w-3xl">
            <div className="flex items-center gap-2 text-sm text-slate-500">
                <button onClick={() => navigate(-1)} className="hover:text-[#175FFA] transition-colors">
                    Back
                </button>
                <span>/</span>
                <Link to="/tasks" className="hover:text-[#175FFA] transition-colors">Tasks</Link>
                <span>/</span>
                <span className="text-slate-900 font-medium">Task #{data.taskId}</span>
            </div>

            <div className="card overflow-hidden">
                <div
                    className="px-6 py-5"
                    style={{ background: "linear-gradient(135deg, #175FFA 0%, #4F8EF7 100%)" }}
                >
                    <div className="flex items-start justify-between gap-3">
                        <div>
                            <div className="text-white/70 text-xs font-semibold uppercase tracking-wide mb-1">
                                Task #{data.taskId}
                            </div>
                            <h2 className="text-lg font-bold text-white leading-snug">{data.title}</h2>
                        </div>
                        <TaskStatusBadge status={data.status} />
                    </div>
                </div>

                <div className="px-6 py-5 border-b" style={{ borderColor: "var(--border)" }}>
                    <div className="text-xs font-semibold text-slate-400 uppercase tracking-wide mb-2">Description</div>
                    <p className="text-sm text-slate-800 whitespace-pre-wrap leading-relaxed">{data.description}</p>
                </div>

                <div className="px-6 py-4">
                    <InfoRow label="Incident">
                        <Link to={`/incidents/${data.incidentId}`} className="text-[#175FFA] hover:underline font-mono">
                            #{data.incidentId}
                        </Link>
                    </InfoRow>
                    <InfoRow label="Assigned To">
                        <UserChip userId={data.assignedTo} userMap={userMap} />
                    </InfoRow>
                    <InfoRow label="Assigned By">
                        <UserChip userId={data.assignedBy} userMap={userMap} />
                    </InfoRow>
                    <InfoRow label="Created">{fmtDate(data.createdDate)}</InfoRow>
                    <InfoRow label="Due Date">{fmtDate(data.dueDate)}</InfoRow>
                </div>
            </div>

            {canUpdateStatus && (
                <div className="card p-6 space-y-4">
                    <h3 className="text-sm font-semibold text-slate-900">Update Status</h3>
                    <div className="flex flex-wrap gap-2">
                        {allowedStatuses.map((s) => (
                            <button
                                key={s}
                                onClick={() => { setNewStatus(s); setUpdateOk(false); setUpdateErr(null); }}
                                className={`px-4 py-2 rounded-lg text-sm font-medium border transition-colors ${newStatus === s ? "bg-[#175FFA] text-white border-[#175FFA]" : "border-[var(--border)] text-slate-600 hover:bg-[#FAFCFF]"}`}
                            >
                                {s === "IN_PROGRESS" ? "In Progress" : s.charAt(0) + s.slice(1).toLowerCase()}
                            </button>
                        ))}
                    </div>
                    {updateErr && (
                        <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                            {updateErr}
                        </div>
                    )}
                    {updateOk && (
                        <div className="flex items-center gap-2 text-sm text-green-700 bg-green-50 border border-green-200 rounded-lg px-3 py-2">
                            Status updated to {newStatus}.
                        </div>
                    )}
                    <button
                        className="btn-primary"
                        onClick={onUpdateStatus}
                        disabled={updating || data.status === newStatus}
                    >
                        {updating ? "Updating..." : "Apply Status"}
                    </button>
                </div>
            )}
        </div>
    );
}
