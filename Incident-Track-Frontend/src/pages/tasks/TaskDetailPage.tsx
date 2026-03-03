import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { tasksApi } from "../../features/tasks/api";
import type { TaskResponseDTO, TaskStatus } from "../../features/tasks/types";
import { useAuth } from "../../context/AuthContext";
import TaskStatusBadge from "../../components/common/TaskStatusBadge";

function InfoRow({ label, children }: { label: string; children: React.ReactNode }) {
    return (
        <div
            className="flex flex-col gap-1 py-2 border-b last:border-0 sm:flex-row sm:items-start sm:justify-between"
            style={{ borderColor: "var(--border)" }}
        >
            <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wide w-full shrink-0 sm:w-28">
                {label}
            </span>
            <span className="text-xs text-slate-800 font-medium text-left sm:text-right">{children}</span>
        </div>
    );
}

function UserChip({ userId, username }: { userId: number; username?: string | null }) {
    if (!username) return <span className="font-mono text-slate-400 text-xs">#{userId}</span>;
    return (
        <span className="text-xs font-medium text-slate-900">
            {username} <span className="text-slate-400 font-mono">#{userId}</span>
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

    const [newStatus, setNewStatus] = useState<TaskStatus>("IN_PROGRESS");
    const [updating, setUpdating] = useState(false);
    const [updateErr, setUpdateErr] = useState<string | null>(null);
    const [updateOk, setUpdateOk] = useState(false);
    const [updatedToStatus, setUpdatedToStatus] = useState<TaskStatus | null>(null);

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
            setNewStatus(res.status === "PENDING" ? "IN_PROGRESS" : res.status === "IN_PROGRESS" ? "COMPLETED" : res.status);
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
        const submittedStatus = newStatus;
        setUpdateErr(null);
        setUpdateOk(false);
        setUpdatedToStatus(null);
        setUpdating(true);
        try {
            await tasksApi.updateStatus(taskId, { status: submittedStatus });
            setUpdatedToStatus(submittedStatus);
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
        const d = new Date(iso);
        return d.toLocaleDateString() + " " + d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    };

    if (loading)
        return <div className="text-xs text-slate-500 py-8 text-center">Loading task…</div>;

    if (err)
        return (
            <div className="card p-6 text-center">
                <p className="text-xs text-red-600 mb-3">{err}</p>
                <button onClick={() => navigate("/tasks")} className="text-xs text-[#175FFA] hover:underline">
                    Back to Tasks
                </button>
            </div>
        );

    if (!data) return null;

    const allowedStatuses: TaskStatus[] = !canUpdateStatus
        ? []
        : data.status === "PENDING"
            ? ["IN_PROGRESS"]
            : data.status === "IN_PROGRESS"
                ? ["COMPLETED"]
                : [];
    const showUpdateSection = canUpdateStatus && (allowedStatuses.length > 0 || updateErr !== null || updateOk);

    return (
        <div className="page-panel space-y-3">
            {/* Breadcrumb */}
            <div className="flex items-center gap-1.5 text-xs text-slate-500">
                <Link to="/tasks" className="hover:text-[#175FFA] transition-colors">Tasks</Link>
                <span>/</span>
                <span className="text-slate-900 font-medium">Task #{data.taskId}</span>
            </div>

            {/* Page heading + back */}
            <div className="flex items-start justify-between gap-3">
                <div>
                    <h2 className="text-base font-semibold text-slate-900 leading-tight">{data.title}</h2>
                    <p className="text-[11px] text-slate-400 mt-0.5">Task #{data.taskId}</p>
                </div>
                <button
                    onClick={() => navigate(-1)}
                    className="inline-flex items-center gap-1 h-8 px-3.5 rounded-[8px] border text-xs font-medium text-slate-700 hover:bg-[#FAFCFF] transition-colors shrink-0"
                    style={{ borderColor: "var(--border)" }}
                >
                    <svg xmlns="http://www.w3.org/2000/svg" className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}><path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" /></svg>
                    Back
                </button>
            </div>

            {/* Detail card */}
            <div className="card p-4">
                {/* Description */}
                <div className="mb-3 pb-3 border-b" style={{ borderColor: "var(--border)" }}>
                    <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wide mb-1">Description</div>
                    <p className="text-xs text-slate-700 whitespace-pre-wrap leading-relaxed">{data.description}</p>
                </div>

                {/* 2-column field grid */}
                <div className="grid grid-cols-1 gap-x-6 gap-y-0 md:grid-cols-2">
                    <InfoRow label="Incident">
                        <Link to={`/incidents/${data.incidentId}?fromTask=true`} className="text-[#175FFA] hover:underline font-mono text-xs">
                            #{data.incidentId}
                        </Link>
                    </InfoRow>
                    <InfoRow label="Status">
                        <TaskStatusBadge status={data.status} />
                    </InfoRow>
                    <InfoRow label="Assigned To">
                        <UserChip userId={data.assignedTo} username={data.assignedToUsername} />
                    </InfoRow>
                    <InfoRow label="Assigned By">
                        <UserChip userId={data.assignedBy} username={data.assignedByUsername} />
                    </InfoRow>
                    <InfoRow label="Created">{fmtDate(data.createdDate)}</InfoRow>
                    <InfoRow label="Due Date">{fmtDate(data.dueDate)}</InfoRow>
                </div>

                {/* Update status — inline, no second card */}
                {showUpdateSection && (
                    <div className="mt-3 pt-3 border-t" style={{ borderColor: "var(--border)" }}>
                        {allowedStatuses.length > 0 && (
                            <>
                                <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wide mb-2">Update Status</div>
                                <div className="flex flex-wrap items-center gap-1.5">
                                    {allowedStatuses.map((s) => (
                                        <button
                                            key={s}
                                            onClick={() => {
                                                setNewStatus(s);
                                                setUpdateOk(false);
                                                setUpdateErr(null);
                                                setUpdatedToStatus(null);
                                            }}
                                            className={`h-7 px-3 rounded-[8px] text-xs font-medium border transition-colors ${newStatus === s
                                                ? "bg-[#175FFA] text-white border-[#175FFA]"
                                                : "border-[var(--border)] text-slate-600 hover:bg-[#FAFCFF]"
                                                }`}
                                        >
                                            {s === "IN_PROGRESS" ? "In Progress" : s.charAt(0) + s.slice(1).toLowerCase()}
                                        </button>
                                    ))}
                                    <button
                                        className="btn-primary h-7 text-xs px-3 ml-auto"
                                        onClick={onUpdateStatus}
                                        disabled={updating}
                                    >
                                        {updating ? "Updating…" : "Apply"}
                                    </button>
                                </div>
                            </>
                        )}
                        {updateErr && (
                            <div className="flex items-center gap-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-[8px] px-3 py-1.5 mt-2">
                                {updateErr}
                            </div>
                        )}
                        {updateOk && updatedToStatus && (
                            <div className="flex items-center gap-2 text-xs text-green-700 bg-green-50 border border-green-200 rounded-[8px] px-3 py-1.5 mt-2">
                                Status updated to {updatedToStatus}.
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
