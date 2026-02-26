import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { tasksApi } from "../../features/tasks/api";
import { authApi } from "../../features/auth/api";
import { incidentsApi } from "../../features/incidents/api";
import type { UserResponseDto } from "../../features/auth/types";
import type { IncidentResponseDTO } from "../../features/incidents/types";

export default function TaskCreatePage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const prefilledIncidentId = searchParams.get("incidentId") ?? "";

    /* form state */
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [incidentId, setIncidentId] = useState(prefilledIncidentId);
    const [assignedTo, setAssignedTo] = useState<string>("");

    /* supporting data */
    const [employees, setEmployees] = useState<UserResponseDto[]>([]);
    const [incidents, setIncidents] = useState<IncidentResponseDTO[]>([]);
    const [loadingData, setLoadingData] = useState(true);

    /* submission */
    const [saving, setSaving] = useState(false);
    const [err, setErr] = useState<string | null>(null);

    /* field-level errors */
    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

    useEffect(() => {
        const fetchSupportingData = async () => {
            setLoadingData(true);
            try {
                // Manager gets employees in their department; Admin gets all
                const [empList, incList] = await Promise.allSettled([
                    authApi.getEmployeesByDepartment(),
                    incidentsApi.listAllAdminManager(),
                ]);

                if (empList.status === "fulfilled") setEmployees(empList.value);
                if (incList.status === "fulfilled") setIncidents(incList.value);
            } finally {
                setLoadingData(false);
            }
        };
        fetchSupportingData();
    }, []);

    const validate = () => {
        const errors: Record<string, string> = {};
        if (!title.trim()) errors.title = "Title is required.";
        if (!description.trim()) errors.description = "Description is required.";
        if (!incidentId || isNaN(Number(incidentId))) errors.incidentId = "Select a valid incident.";
        if (!assignedTo || isNaN(Number(assignedTo))) errors.assignedTo = "Select an assignee.";
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validate()) return;
        setSaving(true);
        setErr(null);
        try {
            const task = await tasksApi.create({
                title: title.trim(),
                description: description.trim(),
                incidentId: Number(incidentId),
                assignedTo: Number(assignedTo),
            });
            navigate(`/tasks/${task.taskId}`);
        } catch (e: any) {
            setErr(e?.response?.data?.message ?? "Failed to create task. Please try again.");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="max-w-2xl space-y-5">
            {/* ── Breadcrumb ── */}
            <div className="flex items-center gap-2 text-sm text-slate-500">
                <Link to="/tasks" className="hover:text-[#175FFA] transition-colors">Tasks</Link>
                <span>/</span>
                <span className="text-slate-900 font-medium">Create Task</span>
            </div>

            {/* ── Form card ── */}
            <div className="card overflow-hidden">
                {/* Header */}
                <div
                    className="px-6 py-5"
                    style={{ background: "linear-gradient(135deg, #175FFA 0%, #4F8EF7 100%)" }}
                >
                    <h1 className="text-lg font-bold text-white">Create New Task</h1>
                    <p className="text-white/70 text-sm mt-0.5">
                        Assign a task to an employee for a specific incident.
                    </p>
                </div>

                <form onSubmit={onSubmit} className="px-6 py-6 space-y-5">
                    {/* Title */}
                    <div>
                        <label className="text-sm font-medium text-slate-700">
                            Title <span className="text-red-500">*</span>
                        </label>
                        <input
                            className={`input mt-1 ${fieldErrors.title ? "border-red-400 focus:ring-red-300" : ""}`}
                            value={title}
                            onChange={(e) => { setTitle(e.target.value); setFieldErrors((p) => ({ ...p, title: "" })); }}
                            placeholder="Short, descriptive task title"
                        />
                        {fieldErrors.title && <p className="text-xs text-red-500 mt-1">{fieldErrors.title}</p>}
                    </div>

                    {/* Description */}
                    <div>
                        <label className="text-sm font-medium text-slate-700">
                            Description <span className="text-red-500">*</span>
                        </label>
                        <textarea
                            className={`input mt-1 h-28 resize-none py-2.5 ${fieldErrors.description ? "border-red-400" : ""}`}
                            value={description}
                            onChange={(e) => { setDescription(e.target.value); setFieldErrors((p) => ({ ...p, description: "" })); }}
                            placeholder="Detailed steps or context for the assignee…"
                        />
                        {fieldErrors.description && <p className="text-xs text-red-500 mt-1">{fieldErrors.description}</p>}
                    </div>

                    {/* Incident selector */}
                    <div>
                        <label className="text-sm font-medium text-slate-700">
                            Linked Incident <span className="text-red-500">*</span>
                        </label>
                        {loadingData ? (
                            <div className="input mt-1 flex items-center text-slate-400 text-sm">Loading incidents…</div>
                        ) : (
                            <select
                                className={`input mt-1 bg-white ${fieldErrors.incidentId ? "border-red-400" : ""}`}
                                value={incidentId}
                                onChange={(e) => { setIncidentId(e.target.value); setFieldErrors((p) => ({ ...p, incidentId: "" })); }}
                            >
                                <option value="">— Select incident —</option>
                                {incidents.map((inc) => (
                                    <option key={inc.incidentId} value={inc.incidentId}>
                                        #{inc.incidentId} — {inc.categoryName} ({inc.status})
                                    </option>
                                ))}
                            </select>
                        )}
                        {fieldErrors.incidentId && <p className="text-xs text-red-500 mt-1">{fieldErrors.incidentId}</p>}
                    </div>

                    {/* Assignee selector */}
                    <div>
                        <label className="text-sm font-medium text-slate-700">
                            Assign To <span className="text-red-500">*</span>
                        </label>
                        {loadingData ? (
                            <div className="input mt-1 flex items-center text-slate-400 text-sm">Loading employees…</div>
                        ) : employees.length === 0 ? (
                            <div className="input mt-1 flex items-center text-slate-400 text-sm">
                                No employees found in your department.
                            </div>
                        ) : (
                            <select
                                className={`input mt-1 bg-white ${fieldErrors.assignedTo ? "border-red-400" : ""}`}
                                value={assignedTo}
                                onChange={(e) => { setAssignedTo(e.target.value); setFieldErrors((p) => ({ ...p, assignedTo: "" })); }}
                            >
                                <option value="">— Select employee —</option>
                                {employees.map((emp) => (
                                    <option key={emp.userId} value={emp.userId}>
                                        {emp.username} (#{emp.userId}) — {emp.email}
                                    </option>
                                ))}
                            </select>
                        )}
                        {fieldErrors.assignedTo && <p className="text-xs text-red-500 mt-1">{fieldErrors.assignedTo}</p>}
                    </div>

                    {/* Global error */}
                    {err && (
                        <div className="flex items-center gap-2 text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg px-3 py-2">
                            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M12 9v2m0 4h.01M21 12A9 9 0 113 12a9 9 0 0118 0z" />
                            </svg>
                            {err}
                        </div>
                    )}

                    {/* Actions */}
                    <div className="flex items-center gap-3 pt-2">
                        <button type="submit" className="btn-primary" disabled={saving || loadingData}>
                            {saving ? "Creating…" : "Create Task"}
                        </button>
                        <button
                            type="button"
                            onClick={() => navigate(-1)}
                            className="h-[46px] px-4 rounded-[10px] border text-sm font-medium text-slate-700 hover:bg-[#FAFCFF] transition-colors"
                            style={{ borderColor: "var(--border)" }}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
