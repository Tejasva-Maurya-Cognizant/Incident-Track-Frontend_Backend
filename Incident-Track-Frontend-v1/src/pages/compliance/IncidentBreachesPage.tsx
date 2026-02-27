import { useCallback, useEffect, useState } from "react";
import { complianceApi } from "../../features/compliance/api";
import type { SlaBreachResponseDto, BreachStatus } from "../../features/compliance/types";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";

// ── Helpers ───────────────────────────────────────────────────────────────────
function fmt(dt: string | null) {
    if (!dt) return "—";
    return new Date(dt).toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "short" });
}

function duration(minutes: number) {
    if (minutes < 60) return `${minutes}m`;
    const h = Math.floor(minutes / 60);
    const m = minutes % 60;
    return m > 0 ? `${h}h ${m}m` : `${h}h`;
}

function BreachStatusBadge({ status }: { status: BreachStatus }) {
    const styles: Record<BreachStatus, string> = {
        OPEN: "bg-[#FEE2E2] text-[#DC2626]",
        RESOLVED: "bg-[#F0FDF4] text-[#15803D]",
    };
    return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${styles[status] ?? "bg-slate-100 text-slate-500"}`}>
            {status}
        </span>
    );
}

function IncidentStatusBadge({ status }: { status: string }) {
    const map: Record<string, string> = {
        OPEN: "bg-[#EEF4FF] text-[#175FFA]",
        IN_PROGRESS: "bg-[#FEF9C3] text-[#A16207]",
        RESOLVED: "bg-[#F0FDF4] text-[#15803D]",
        CLOSED: "bg-slate-100 text-slate-500",
        WITHDRAWN: "bg-[#FEE2E2] text-[#DC2626]",
    };
    return (
        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${map[status] ?? "bg-slate-100 text-slate-500"}`}>
            {status.replace(/_/g, " ")}
        </span>
    );
}

// ── Detail drawer ─────────────────────────────────────────────────────────────
function DetailDrawer({ breach, onClose }: { breach: SlaBreachResponseDto; onClose: () => void }) {
    const overdue = breach.breachMinutes;

    return (
        <div className="fixed inset-0 z-50 flex justify-end" style={{ background: "rgba(0,0,0,0.35)" }} onClick={onClose}>
            <div
                className="card h-full w-full max-w-sm p-5 overflow-y-auto space-y-4 rounded-r-none"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="flex items-start justify-between">
                    <div>
                        <h3 className="text-sm font-semibold text-slate-900">SLA Breach Detail</h3>
                        <p className="text-xs text-slate-400 mt-0.5">Breach #{breach.breachId}</p>
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

                <dl className="space-y-3 text-xs">
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Breach Status</dt>
                        <dd className="mt-0.5"><BreachStatusBadge status={breach.breachStatus as BreachStatus} /></dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Incident</dt>
                        <dd className="mt-0.5 flex items-center gap-2">
                            <span className="text-slate-800 font-medium">#{breach.incidentId}</span>
                            <IncidentStatusBadge status={breach.incidentStatus} />
                        </dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">SLA Due At</dt>
                        <dd className="mt-0.5 text-slate-800">{fmt(breach.slaDueAt)}</dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Breached At</dt>
                        <dd className="mt-0.5 text-slate-800">{fmt(breach.breachedAt)}</dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Overdue By</dt>
                        <dd className="mt-0.5 text-red-600 font-semibold">{duration(overdue)}</dd>
                    </div>
                    <div>
                        <dt className="text-slate-400 font-medium uppercase tracking-wide text-[10px]">Reason</dt>
                        <dd className="mt-1 text-slate-700 whitespace-pre-wrap rounded-[6px] bg-[#F8FAFD] border p-2 leading-relaxed" style={{ borderColor: "var(--border)" }}>
                            {breach.reason ?? "No reason recorded."}
                        </dd>
                    </div>
                </dl>
            </div>
        </div>
    );
}

// ── Main page ─────────────────────────────────────────────────────────────────
const PAGE_SIZE_OPTIONS = [10, 20, 50, 100];
const SORT_DEFAULT = { sortBy: "breachedAt", sortDir: "desc" as const };

export default function IncidentBreachesPage() {
    // All records fetched from backend (sorted, no server-side paging)
    const [allBreaches, setAllBreaches] = useState<SlaBreachResponseDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Sort (server-side)
    const [sortBy, setSortBy] = useState(SORT_DEFAULT.sortBy);
    const [sortDir, setSortDir] = useState<"asc" | "desc">(SORT_DEFAULT.sortDir);

    // Client-side filters
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState<BreachStatus | "">("");
    const [incidentStatusFilter, setIncidentStatusFilter] = useState("");

    // Client-side pagination
    const [page, setPage] = useState(0);
    const [pageSize, setPageSize] = useState(20);

    const [selected, setSelected] = useState<SlaBreachResponseDto | null>(null);

    // Fetch all records sorted; search/filter/pagination are client-side
    const load = useCallback(async (sb: string, sd: string) => {
        setLoading(true);
        setError(null);
        try {
            const data = await complianceApi.getBreachesPaged({ page: 0, size: 10000, sortBy: sb, sortDir: sd as "asc" | "desc" });
            setAllBreaches(data.content);
        } catch (e: any) {
            setError(e?.response?.data?.message ?? "Failed to load breach records");
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => { load(sortBy, sortDir); }, [load, sortBy, sortDir]);

    const handleSort = (field: string) => {
        const newDir = sortBy === field && sortDir === "asc" ? "desc" : "asc";
        setSortBy(field);
        setSortDir(newDir);
        setPage(0);
    };

    // Reset to page 0 when filters/search change
    const handleSearch = (v: string) => { setSearch(v); setPage(0); };
    const handleStatusFilter = (v: BreachStatus | "") => { setStatusFilter(v); setPage(0); };
    const handleIncidentStatusFilter = (v: string) => { setIncidentStatusFilter(v); setPage(0); };

    // Client-side filter across ALL records
    const filtered = allBreaches.filter((b) => {
        if (statusFilter && b.breachStatus !== statusFilter) return false;
        if (incidentStatusFilter && b.incidentStatus !== incidentStatusFilter) return false;
        if (search) {
            const q = search.toLowerCase();
            if (
                !String(b.breachId).includes(q) &&
                !String(b.incidentId).includes(q)
            ) return false;
        }
        return true;
    });

    // Client-side pagination over filtered results
    const totalFiltered = filtered.length;
    const totalPages = Math.max(1, Math.ceil(totalFiltered / pageSize));
    const safePage = Math.min(page, totalPages - 1);
    const pageSlice = filtered.slice(safePage * pageSize, safePage * pageSize + pageSize);

    const hasFilters = search || statusFilter || incidentStatusFilter;

    // Fake PageParams shape for SortableHeader compatibility
    const sortParams = { sortBy, sortDir };

    return (
        <div className="flex flex-col h-full gap-3">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-base font-semibold text-slate-900">Incident SLA Breaches</h1>
                    <p className="text-xs text-slate-500 mt-0.5">Incidents that exceeded their SLA deadline</p>
                </div>
                <span className="text-xs text-slate-400 bg-[#F8FAFD] border px-2 py-1 rounded-[6px]" style={{ borderColor: "var(--border)" }}>
                    {allBreaches.length} breaches
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
                        placeholder="Search by Breach ID / Incident ID…"
                        value={search}
                        onChange={(e) => handleSearch(e.target.value)}
                    />
                </div>

                {/* Breach status filter */}
                <select
                    className="input h-8 text-xs bg-white flex-1 min-w-0"
                    value={statusFilter}
                    onChange={(e) => handleStatusFilter(e.target.value as BreachStatus | "")}
                >
                    <option value="">All Breach Statuses</option>
                    <option value="OPEN">OPEN</option>
                    <option value="RESOLVED">RESOLVED</option>
                </select>

                {/* Incident status filter */}
                <select
                    className="input h-8 text-xs bg-white flex-1 min-w-0"
                    value={incidentStatusFilter}
                    onChange={(e) => handleIncidentStatusFilter(e.target.value)}
                >
                    <option value="">All Incident Statuses</option>
                    <option value="OPEN">OPEN</option>
                    <option value="IN_PROGRESS">IN PROGRESS</option>
                    <option value="RESOLVED">RESOLVED</option>
                    <option value="CLOSED">CLOSED</option>
                    <option value="WITHDRAWN">WITHDRAWN</option>
                </select>

                {/* Result count */}
                <span className="shrink-0 text-xs text-slate-400 whitespace-nowrap">
                    {totalFiltered} result{totalFiltered !== 1 ? "s" : ""}
                </span>

                {/* Clear filters */}
                {hasFilters && (
                    <button
                        className="shrink-0 h-8 px-3 rounded-[8px] bg-white border text-xs text-slate-600 hover:bg-[#FAFCFF] whitespace-nowrap"
                        style={{ borderColor: "var(--border)" }}
                        onClick={() => { handleSearch(""); handleStatusFilter(""); handleIncidentStatusFilter(""); }}
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
                    <table className="w-full text-xs border-collapse min-w-[750px]">
                        <thead>
                            <tr style={{ position: "sticky", top: 0, zIndex: 5, background: "#F8FAFD" }}>
                                <SortableHeader label="Breach ID" field="breachId" sortBy={sortParams.sortBy} sortDir={sortParams.sortDir} onSort={handleSort} className="px-3 py-2 w-24" />
                                <SortableHeader label="Incident" field="incidentId" sortBy={sortParams.sortBy} sortDir={sortParams.sortDir} onSort={handleSort} className="px-2 py-2 w-24" />
                                <SortableHeader label="Inc. Status" field="incidentStatus" sortBy={sortParams.sortBy} sortDir={sortParams.sortDir} onSort={handleSort} className="px-2 py-2" />
                                <SortableHeader label="SLA Due" field="slaDueAt" sortBy={sortParams.sortBy} sortDir={sortParams.sortDir} onSort={handleSort} className="px-2 py-2 w-40" />
                                <SortableHeader label="Breached At" field="breachedAt" sortBy={sortParams.sortBy} sortDir={sortParams.sortDir} onSort={handleSort} className="px-2 py-2 w-40" />
                                <SortableHeader label="Overdue" field="breachMinutes" sortBy={sortParams.sortBy} sortDir={sortParams.sortDir} onSort={handleSort} className="px-2 py-2 w-24" />
                                <SortableHeader label="Status" field="breachStatus" sortBy={sortParams.sortBy} sortDir={sortParams.sortDir} onSort={handleSort} className="px-2 py-2 w-24" />
                                <th className="text-left px-2 py-2 text-xs uppercase tracking-wide text-slate-500">Reason</th>
                                <th className="px-2 py-2 w-16" />
                            </tr>
                        </thead>
                        <tbody>
                            {loading && (
                                <tr>
                                    <td colSpan={9} className="text-center py-10 text-slate-400">Loading…</td>
                                </tr>
                            )}
                            {!loading && pageSlice.length === 0 && (
                                <tr>
                                    <td colSpan={9} className="text-center py-10 text-slate-400">No breach records found.</td>
                                </tr>
                            )}
                            {!loading && pageSlice.map((b, i) => (
                                <tr
                                    key={b.breachId}
                                    className="border-t hover:bg-[#FAFCFF] cursor-pointer"
                                    style={{ borderColor: "var(--border)", background: i % 2 === 0 ? "white" : "#FAFCFF" }}
                                    onClick={() => setSelected(b)}
                                >
                                    <td className="px-3 py-2 font-mono text-slate-500">#{b.breachId}</td>
                                    <td className="px-2 py-2 text-slate-700 font-medium">#{b.incidentId}</td>
                                    <td className="px-2 py-2"><IncidentStatusBadge status={b.incidentStatus} /></td>
                                    <td className="px-2 py-2 text-slate-600 whitespace-nowrap">{fmt(b.slaDueAt)}</td>
                                    <td className="px-2 py-2 text-slate-600 whitespace-nowrap">{fmt(b.breachedAt)}</td>
                                    <td className="px-2 py-2 font-semibold text-red-600">{duration(b.breachMinutes)}</td>
                                    <td className="px-2 py-2"><BreachStatusBadge status={b.breachStatus as BreachStatus} /></td>
                                    <td className="px-2 py-2 text-slate-600 max-w-[180px] truncate">{b.reason ?? <span className="text-slate-400">—</span>}</td>
                                    <td className="px-2 py-2 text-right">
                                        <button
                                            className="h-6 px-2 rounded-[5px] bg-white border text-[10px] text-slate-600 hover:bg-[#FAFCFF]"
                                            style={{ borderColor: "var(--border)" }}
                                            onClick={(e) => { e.stopPropagation(); setSelected(b); }}
                                        >
                                            View
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <Pagination
                    page={safePage}
                    totalPages={totalPages}
                    totalElements={totalFiltered}
                    size={pageSize}
                    onPageChange={(p) => setPage(p)}
                    onSizeChange={(s) => { setPageSize(s); setPage(0); }}
                    pageSizeOptions={PAGE_SIZE_OPTIONS}
                />
            </div>

            {/* Detail drawer */}
            {selected && <DetailDrawer breach={selected} onClose={() => setSelected(null)} />}
        </div>
    );
}