import { useCallback, useEffect, useState } from "react";
import { complianceApi } from "../../features/compliance/api";
import type { AuditLogResponseDto, ActionType } from "../../features/compliance/types";
import ModalWindow from "../../components/common/ModalWindow";
import type { PageParams } from "../../types/pagination";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";
import {
    TableBodyRow,
    TableHeaderCell,
    TableIdCell,
    TABLE_HEADER_ROW_CLASS,
    TABLE_HEADER_ROW_STYLE,
} from "../../components/common/TablePrimitives";

// ── Helpers ───────────────────────────────────────────────────────────────────
const ACTION_TYPES: ActionType[] = [
    "INCIDENT_CREATED",
    "INCIDENT_UPDATED",
    "INCIDENT_STATUS_CHANGED",
    "INCIDENT_WITHDRAWN",
    "TASK_CREATED",
    "TASK_ASSIGNED",
    "TASK_STATUS_CHANGED",
    "CATEGORY_CHANGED",
    "NOTE_ADDED",
];

const ACTION_COLOR: Record<ActionType, string> = {
    INCIDENT_CREATED: "bg-[#EEF4FF] text-[#175FFA]",
    INCIDENT_UPDATED: "bg-[#F0FDF4] text-[#15803D]",
    INCIDENT_STATUS_CHANGED: "bg-[#FEF9C3] text-[#A16207]",
    INCIDENT_WITHDRAWN: "bg-[#FEE2E2] text-[#DC2626]",
    TASK_CREATED: "bg-[#EEF4FF] text-[#6D28D9]",
    TASK_ASSIGNED: "bg-[#F0FDF4] text-[#0891B2]",
    TASK_STATUS_CHANGED: "bg-[#FEF9C3] text-[#D97706]",
    CATEGORY_CHANGED: "bg-slate-100 text-slate-600",
    NOTE_ADDED: "bg-slate-100 text-slate-500",
};

function ActionBadge({ action }: { action: ActionType }) {
    return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold whitespace-nowrap ${ACTION_COLOR[action] ?? "bg-slate-100 text-slate-500"}`}>
            {action.replace(/_/g, " ")}
        </span>
    );
}

function fmt(dt: string | null) {
    if (!dt) return "—";
    const d = new Date(dt);
    return d.toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "short" });
}

// ── Detail drawer ─────────────────────────────────────────────────────────────
function DetailModal({ log, onClose }: { log: AuditLogResponseDto; onClose: () => void }) {
    return (
        <ModalWindow
            title="Audit Log Detail"
            subtitle={`Log #${log.logId}`}
            onClose={onClose}
            maxWidthClassName="max-w-xl"
        >
            <dl className="space-y-3 text-xs">
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Action</dt>
                        <dd className="mt-0.5"><ActionBadge action={log.actionType} /></dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Timestamp</dt>
                        <dd className="mt-0.5 text-slate-800">{fmt(log.timestamp)}</dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Performed By</dt>
                        <dd className="mt-0.5 text-slate-800">{log.username ?? "—"} {log.userId ? <span className="text-slate-400">(#{log.userId})</span> : ""}</dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Incident ID</dt>
                        <dd className="mt-0.5 text-slate-800">{log.incidentId != null ? `#${log.incidentId}` : "—"}</dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Details</dt>
                        <dd className="mt-1 text-slate-700 whitespace-pre-wrap rounded-[6px] bg-[#F8FAFD] border p-2 leading-relaxed" style={{ borderColor: "var(--border)" }}>
                            {log.details ?? "No additional details."}
                        </dd>
                    </div>
                </dl>
        </ModalWindow>
    );
}

// ── Main page ─────────────────────────────────────────────────────────────────
const DEFAULT_PARAMS: PageParams = { page: 0, size: 20, sortBy: "timestamp", sortDir: "desc" };

export default function AdminAuditLogPage() {
    const [logs, setLogs] = useState<AuditLogResponseDto[]>([]);
    const [total, setTotal] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);

    // Client-side filters
    const [search, setSearch] = useState("");
    const [actionFilter, setActionFilter] = useState<ActionType | "">("");

    const [selected, setSelected] = useState<AuditLogResponseDto | null>(null);

    const load = useCallback(async (p: PageParams, action: ActionType | "") => {
        setLoading(true);
        setError(null);
        try {
            let data;
            if (action) {
                data = await complianceApi.getAuditLogsByActionTypePaged(action, p);
            } else {
                data = await complianceApi.getAuditLogsPaged(p);
            }
            setLogs(data.content);
            setTotal(data.totalElements);
            setTotalPages(data.totalPages);
        } catch (e: any) {
            setError(e?.response?.data?.message ?? "Failed to load audit logs");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { load(params, actionFilter); }, [load, params, actionFilter]);

    const handleSort = (field: string) => {
        setParams((prev) => ({
            ...prev,
            page: 0,
            sortBy: field,
            sortDir: prev.sortBy === field && prev.sortDir === "asc" ? "desc" : "asc",
        }));
    };

    const handleActionFilter = (val: ActionType | "") => {
        setActionFilter(val);
        setParams((prev) => ({ ...prev, page: 0 }));
    };

    // client-side keyword search on the loaded page
    const filtered = logs.filter((l) => {
        if (!search) return true;
        const q = search.toLowerCase();
        return (
            String(l.logId).includes(q) ||
            (l.username ?? "").toLowerCase().includes(q) ||
            (l.details ?? "").toLowerCase().includes(q) ||
            String(l.incidentId ?? "").includes(q)
        );
    });

    return (
        <div className="flex flex-col h-full gap-3">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-base font-semibold text-slate-900">Audit Logs</h1>
                    <p className="text-xs text-slate-500 mt-0.5">Full history of all system actions</p>
                </div>
                <span className="text-xs text-slate-400 bg-[#F8FAFD] border px-2 py-1 rounded-[6px]" style={{ borderColor: "var(--border)" }}>
                    {total} records
                </span>
            </div>

            {/* Toolbar */}
            <div className="card p-2 flex items-center gap-2 flex-nowrap">
                {/* Search */}
                <div className="relative flex-[2] min-w-0">
                    <svg className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400 pointer-events-none" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35M16.65 16.65A7.5 7.5 0 1 0 4.5 4.5a7.5 7.5 0 0 0 12.15 12.15z" />
                    </svg>
                    <input
                        className="input pl-8 h-8 text-xs w-full"
                        placeholder="Search by user, incident ID, details…"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />
                </div>

                {/* Action type filter */}
                <select
                    className="input h-8 text-xs bg-white flex-1 min-w-0"
                    value={actionFilter}
                    onChange={(e) => handleActionFilter(e.target.value as ActionType | "")}
                >
                    <option value="">All Action Types</option>
                    {ACTION_TYPES.map((a) => (
                        <option key={a} value={a}>{a.replace(/_/g, " ")}</option>
                    ))}
                </select>

                {/* Result count */}
                <span className="shrink-0 text-xs text-slate-400 whitespace-nowrap">
                    {filtered.length} result{filtered.length !== 1 ? "s" : ""}
                </span>

                {/* Clear filters */}
                {(search || actionFilter) && (
                    <button
                        className="shrink-0 h-8 px-3 rounded-[8px] bg-white border text-xs text-slate-600 hover:bg-[#FAFCFF] whitespace-nowrap"
                        style={{ borderColor: "var(--border)" }}
                        onClick={() => { setSearch(""); handleActionFilter(""); }}
                    >
                        Clear filters
                    </button>
                )}
            </div>

            {/* Table card */}
            <div className="card flex flex-col flex-1 overflow-hidden">
                {error && (
                    <div className="p-3 text-xs text-red-600 border-b" style={{ borderColor: "var(--border)" }}>{error}</div>
                )}

                <div className="overflow-x-auto flex-1">
                    <table className="w-full text-xs border-collapse min-w-[700px]">
                        <thead>
                            <tr className={TABLE_HEADER_ROW_CLASS} style={TABLE_HEADER_ROW_STYLE}>
                                <SortableHeader label="Log ID" field="logId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-3 py-2 w-20" />
                                <SortableHeader label="Timestamp" field="timestamp" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-2 py-2 w-44" />
                                <SortableHeader label="Action" field="actionType" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-2 py-2" />
                                <TableHeaderCell>User</TableHeaderCell>
                                <SortableHeader label="Incident" field="incidentId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="px-2 py-2 w-24" />
                                <TableHeaderCell className="w-64">Details</TableHeaderCell>
                                <TableHeaderCell className="w-16" />
                            </tr>
                        </thead>
                        <tbody>
                            {loading && (
                                <tr>
                                    <td colSpan={7} className="text-center py-10 text-slate-400">Loading…</td>
                                </tr>
                            )}
                            {!loading && filtered.length === 0 && (
                                <tr>
                                    <td colSpan={7} className="text-center py-10 text-slate-400">No audit logs found.</td>
                                </tr>
                            )}
                            {!loading && filtered.map((log, i) => (
                                <TableBodyRow
                                    key={log.logId}
                                    index={i}
                                    onClick={() => setSelected(log)}
                                >
                                    <TableIdCell id={log.logId} className="px-3" />
                                    <td className="px-2 py-2 text-slate-600 whitespace-nowrap">{fmt(log.timestamp)}</td>
                                    <td className="px-2 py-2"><ActionBadge action={log.actionType} /></td>
                                    <td className="px-2 py-2 text-slate-700">{log.username ?? <span className="text-slate-400">—</span>}</td>
                                    <TableIdCell id={log.incidentId} />
                                    <td className="px-2 py-2 text-slate-600 max-w-[200px] truncate">{log.details ?? <span className="text-slate-400">—</span>}</td>
                                    <td className="px-2 py-2 text-right">
                                        <button
                                            className="h-6 px-2 rounded-[5px] bg-white border text-[10px] text-slate-600 hover:bg-[#FAFCFF]"
                                            style={{ borderColor: "var(--border)" }}
                                            onClick={(e) => { e.stopPropagation(); setSelected(log); }}
                                        >
                                            View
                                        </button>
                                    </td>
                                </TableBodyRow>
                            ))}
                        </tbody>
                    </table>
                </div>

                <Pagination
                    page={params.page}
                    totalPages={totalPages}
                    totalElements={total}
                    size={params.size}
                    onPageChange={(p) => setParams((prev) => ({ ...prev, page: p }))}
                    onSizeChange={(s) => setParams((prev) => ({ ...prev, page: 0, size: s }))}
                    pageSizeOptions={[10, 20, 50, 100]}
                />
            </div>

            {/* Detail modal */}
            {selected && <DetailModal log={selected} onClose={() => setSelected(null)} />}
        </div>
    );
}
