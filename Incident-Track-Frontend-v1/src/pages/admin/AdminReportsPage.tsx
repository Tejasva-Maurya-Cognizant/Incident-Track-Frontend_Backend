import { useCallback, useEffect, useState } from "react";
import { reportingApi } from "../../features/reporting/api";
import { departmentsApi } from "../../features/departments/api";
import type {
    ReportResponseDto,
    DepartmentPerformanceDto,
    TrendPointDto,
    TrendBucket,
    ReportType,
} from "../../features/reporting/types";
import type { DepartmentResponseDto } from "../../features/departments/types";
import {
    ChartCard,
    PieChartWidget,
    TrendAreaChart,
    DeptPerformanceBarChart,
    SlaComplianceBar,
    StatPill,
} from "../../components/charts/ReportCharts";

// ── helpers ───────────────────────────────────────────────────────────────────
const today = () => new Date().toISOString().slice(0, 10);
const startOfMonth = () => {
    const d = new Date(); d.setDate(1);
    return d.toISOString().slice(0, 10);
};
const startOfYear = () => {
    const d = new Date(); d.setMonth(0); d.setDate(1);
    return d.toISOString().slice(0, 10);
};

function fmtDt(dt: string) {
    return new Date(dt).toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "short" });
}
function fmtDate(d: string) {
    return new Date(d).toLocaleDateString("en-IN", { dateStyle: "medium" });
}

type Tab = "overview" | "trends" | "departments" | "history";

// ── Sort icon helper ─────────────────────────────────────────────────────────
function SortIcon({ field, current, dir }: { field: string; current: string; dir: "asc" | "desc" }) {
    if (current !== field) return <span className="ml-1 text-slate-300">⇅</span>;
    return <span className="ml-1 text-[#1E6FD9]">{dir === "asc" ? "▲" : "▼"}</span>;
}

// ── Report Detail Drawer ──────────────────────────────────────────────────────
function ReportDetailDrawer({ report, onClose }: { report: ReportResponseDto; onClose: () => void }) {
    const REPORT_TYPE_COLOR: Record<string, string> = {
        VOLUME_TREND: "bg-[#EEF4FF] text-[#175FFA]",
        SLA_COMPLIANCE: "bg-[#FEF9C3] text-[#A16207]",
        DEPARTMENT_PERFORMANCE: "bg-[#F0FDF4] text-[#15803D]",
        PERIOD_REPORT: "bg-slate-100 text-slate-600",
        MONTHLY_DISTRIBUTION: "bg-[#FEF3C7] text-[#D97706]",
    };

    const series = report.series as any;
    const isTrend = Array.isArray(series) && series.length > 0 && "incidentCount" in (series[0] ?? {}) && "label" in (series[0] ?? {});
    const isDeptPerf = Array.isArray(series) && series.length > 0 && "departmentName" in (series[0] ?? {});
    const isSlaSummary = series && !Array.isArray(series) && "slaComplianceRate" in series;

    return (
        <>
            {/* Backdrop */}
            <div className="fixed inset-0 bg-black/30 z-40" onClick={onClose} />
            {/* Drawer */}
            <div className="fixed right-0 top-0 h-full w-full max-w-lg bg-white z-50 shadow-2xl flex flex-col overflow-hidden">
                {/* Header */}
                <div className="flex items-center justify-between px-5 py-4 border-b" style={{ borderColor: "var(--border)" }}>
                    <div>
                        <div className="flex items-center gap-2">
                            <span className="text-sm font-semibold text-slate-900">Report #{report.reportId}</span>
                            <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${REPORT_TYPE_COLOR[report.reportType] ?? "bg-slate-100 text-slate-500"}`}>
                                {report.reportType.replace(/_/g, " ")}
                            </span>
                        </div>
                        <div className="text-[11px] text-slate-400 mt-0.5">
                            {fmtDate(report.startDate)} → {fmtDate(report.endDate)} &nbsp;·&nbsp; Generated {fmtDt(report.generatedAt)}
                        </div>
                    </div>
                    <button onClick={onClose} className="p-1.5 rounded-[6px] hover:bg-slate-100 text-slate-400 hover:text-slate-700 transition-colors">
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                    </button>
                </div>

                {/* Body */}
                <div className="flex-1 overflow-y-auto p-5 space-y-4">
                    {/* Scope info */}
                    <div className="text-xs text-slate-500">
                        <span className="font-medium text-slate-700">Scope:</span> {report.scope}{report.scopeRefId != null ? ` #${report.scopeRefId}` : ""}
                    </div>

                    {/* Stat pills */}
                    <div className="grid grid-cols-2 gap-2">
                        <StatPill label="Total Incidents" value={report.incidentCount} color="#3B82F6" />
                        <StatPill label="Resolved" value={report.resolvedIncidentCount} color="#10B981" />
                        <StatPill label="SLA Breaches" value={report.slaBreachedCount} color="#EF4444" />
                        <StatPill
                            label="SLA Compliance"
                            value={`${report.slaComplianceRate?.toFixed(1)}%`}
                            color={report.slaComplianceRate >= 90 ? "#10B981" : report.slaComplianceRate >= 70 ? "#F59E0B" : "#EF4444"}
                            sub={`Avg ${report.averageResolutionTimeHours?.toFixed(1)}h resolution`}
                        />
                    </div>

                    {/* SLA compliance bar */}
                    <SlaComplianceBar rate={report.slaComplianceRate} label="SLA Compliance Rate" />

                    {/* Series chart */}
                    {isTrend && (
                        <ChartCard title="Volume Trend" height={220}>
                            <TrendAreaChart series={series as TrendPointDto[]} />
                        </ChartCard>
                    )}

                    {isDeptPerf && (
                        <ChartCard title="Department Performance" height={220}>
                            <DeptPerformanceBarChart data={series} />
                        </ChartCard>
                    )}

                    {isDeptPerf && (
                        <div className="rounded-[10px] border overflow-hidden" style={{ borderColor: "var(--border)" }}>
                            <table className="w-full text-xs border-collapse">
                                <thead>
                                    <tr style={{ background: "#F8FAFD" }}>
                                        <th className="text-left px-3 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Department</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Total</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Resolved</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Breaches</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">SLA %</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {series.map((d: any, i: number) => (
                                        <tr key={d.departmentId ?? i} className="border-t" style={{ borderColor: "var(--border)", background: i % 2 === 0 ? "white" : "#FAFCFF" }}>
                                            <td className="px-3 py-2 font-medium text-slate-800">{d.departmentName}</td>
                                            <td className="px-2 py-2 text-right text-slate-700">{d.incidentCount}</td>
                                            <td className="px-2 py-2 text-right text-green-600">{d.resolvedIncidentCount}</td>
                                            <td className="px-2 py-2 text-right text-red-500">{d.slaBreachedCount}</td>
                                            <td className="px-2 py-2 text-right">
                                                <span className={`font-semibold ${d.slaComplianceRate >= 90 ? "text-green-600" : d.slaComplianceRate >= 70 ? "text-amber-600" : "text-red-500"}`}>
                                                    {d.slaComplianceRate?.toFixed(1)}%
                                                </span>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {isTrend && (
                        <div className="rounded-[10px] border overflow-hidden" style={{ borderColor: "var(--border)" }}>
                            <table className="w-full text-xs border-collapse">
                                <thead>
                                    <tr style={{ background: "#F8FAFD" }}>
                                        <th className="text-left px-3 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Period</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Incidents</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">SLA Breaches</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {(series as TrendPointDto[]).map((p, i) => (
                                        <tr key={i} className="border-t" style={{ borderColor: "var(--border)", background: i % 2 === 0 ? "white" : "#FAFCFF" }}>
                                            <td className="px-3 py-2 text-slate-700">{p.label}</td>
                                            <td className="px-2 py-2 text-right text-slate-800">{p.incidentCount}</td>
                                            <td className="px-2 py-2 text-right text-red-500">{p.slaBreachedCount}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {isSlaSummary && (
                        <div className="grid grid-cols-2 gap-2">
                            <StatPill label="Open" value={series.openCount} color="#3B82F6" />
                            <StatPill label="In Progress" value={series.inProgressCount} color="#F59E0B" />
                            <StatPill label="Cancelled" value={series.cancelledCount} color="#94A3B8" />
                        </div>
                    )}

                    {!isTrend && !isDeptPerf && !isSlaSummary && (
                        <div className="text-xs text-slate-400 py-4 text-center">No series data available for this report.</div>
                    )}
                </div>
            </div>
        </>
    );
}

// ── Report History Table ──────────────────────────────────────────────────────
type HistSortField = "reportId" | "incidentCount" | "resolvedIncidentCount" | "slaBreachedCount" | "slaComplianceRate" | "generatedAt";

interface ReportHistoryTableProps {
    reports: ReportResponseDto[];
    onView: (r: ReportResponseDto) => void;
}

function ReportHistoryTable({ reports, onView }: Pick<ReportHistoryTableProps, "reports" | "onView">) {
    const [sortField, setSortField] = useState<HistSortField>("generatedAt");
    const [sortDir, setSortDir] = useState<"asc" | "desc">("desc");

    const REPORT_TYPE_COLOR: Record<string, string> = {
        VOLUME_TREND: "bg-[#EEF4FF] text-[#175FFA]",
        SLA_COMPLIANCE: "bg-[#FEF9C3] text-[#A16207]",
        DEPARTMENT_PERFORMANCE: "bg-[#F0FDF4] text-[#15803D]",
        PERIOD_REPORT: "bg-slate-100 text-slate-600",
        MONTHLY_DISTRIBUTION: "bg-[#FEF3C7] text-[#D97706]",
    };

    const handleSort = (field: HistSortField) => {
        if (sortField === field) setSortDir((d) => (d === "asc" ? "desc" : "asc"));
        else { setSortField(field); setSortDir("asc"); }
    };

    const filtered = [...reports].sort((a, b) => {
        let av: any = a[sortField];
        let bv: any = b[sortField];
        if (sortField === "generatedAt") { av = new Date(av).getTime(); bv = new Date(bv).getTime(); }
        if (sortField === "reportId") { av = Number(av); bv = Number(bv); }
        if (av < bv) return sortDir === "asc" ? -1 : 1;
        if (av > bv) return sortDir === "asc" ? 1 : -1;
        return 0;
    });

    const thSort = (field: HistSortField, label: string, align: "left" | "right" = "right") => (
        <th
            className={`px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px] cursor-pointer select-none hover:text-slate-800 ${align === "right" ? "text-right" : "text-left"}`}
            onClick={() => handleSort(field)}
        >
            {label}<SortIcon field={field} current={sortField} dir={sortDir} />
        </th>
    );

    return (
        <div className="overflow-x-auto h-full">
            {filtered.length === 0 ? (
                <div className="text-xs text-slate-400 py-6 text-center">No reports match the selected filters.</div>
            ) : (
                <table className="w-full text-xs border-collapse min-w-[780px]">
                    <thead>
                        <tr className="border-b" style={{ borderColor: "var(--border)", background: "#F8FAFD" }}>
                            {thSort("reportId", "ID", "left")}
                            <th className="text-left px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Type</th>
                            <th className="text-left px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Scope</th>
                            <th className="text-left px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Period</th>
                            {thSort("incidentCount", "Incidents")}
                            {thSort("resolvedIncidentCount", "Resolved")}
                            {thSort("slaBreachedCount", "Breaches")}
                            {thSort("slaComplianceRate", "SLA %")}
                            {thSort("generatedAt", "Generated", "left")}
                            <th className="px-2 py-2"></th>
                        </tr>
                    </thead>
                    <tbody>
                        {filtered.map((r, i) => (
                            <tr
                                key={r.reportId}
                                className="border-t hover:bg-[#F0F7FF] transition-colors"
                                style={{ borderColor: "var(--border)", background: i % 2 === 0 ? "white" : "#FAFCFF" }}
                            >
                                <td className="px-3 py-2 font-mono text-slate-400">#{r.reportId}</td>
                                <td className="px-2 py-2">
                                    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold ${REPORT_TYPE_COLOR[r.reportType] ?? "bg-slate-100 text-slate-500"}`}>
                                        {r.reportType.replace(/_/g, " ")}
                                    </span>
                                </td>
                                <td className="px-2 py-2 text-slate-600">{r.scope}{r.scopeRefId != null ? ` #${r.scopeRefId}` : ""}</td>
                                <td className="px-2 py-2 text-slate-500 whitespace-nowrap">{fmtDate(r.startDate)} → {fmtDate(r.endDate)}</td>
                                <td className="px-2 py-2 text-right font-medium text-slate-800">{r.incidentCount}</td>
                                <td className="px-2 py-2 text-right text-green-600">{r.resolvedIncidentCount}</td>
                                <td className="px-2 py-2 text-right text-red-500">{r.slaBreachedCount}</td>
                                <td className="px-2 py-2 text-right">
                                    <span className={`font-semibold ${r.slaComplianceRate >= 90 ? "text-green-600" : r.slaComplianceRate >= 70 ? "text-amber-600" : "text-red-500"}`}>
                                        {r.slaComplianceRate?.toFixed(1)}%
                                    </span>
                                </td>
                                <td className="px-2 py-2 text-slate-500 whitespace-nowrap">{fmtDt(r.generatedAt)}</td>
                                <td className="px-2 py-2">
                                    <button
                                        onClick={() => onView(r)}
                                        className="h-6 px-2.5 rounded-[6px] text-[10px] font-medium border border-[#1E6FD9] text-[#1E6FD9] hover:bg-[#EEF4FF] transition-colors whitespace-nowrap"
                                    >
                                        View
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

// ── Generate Report Panel ─────────────────────────────────────────────────────
interface GeneratePanelProps {
    departments: DepartmentResponseDto[];
    onGenerated: () => void;
}

type GenType = "global-trend" | "dept-trend" | "dept-sla" | "depts-perf";

function GeneratePanel({ departments, onGenerated }: GeneratePanelProps) {
    const [type, setType] = useState<GenType>("global-trend");
    const [start, setStart] = useState(startOfYear());
    const [end, setEnd] = useState(today());
    const [deptId, setDeptId] = useState<number>(departments[0]?.departmentId ?? 0);
    const [bucket, setBucket] = useState<TrendBucket>("DAILY");
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState<ReportResponseDto | null>(null);
    const [error, setError] = useState<string | null>(null);

    const needsDept = type === "dept-trend" || type === "dept-sla";
    const needsBucket = type === "global-trend" || type === "dept-trend";

    const generate = async () => {
        setLoading(true);
        setError(null);
        setResult(null);
        try {
            let data: ReportResponseDto;
            if (type === "global-trend") {
                data = await reportingApi.getGlobalVolumeTrend(start, end, bucket);
            } else if (type === "dept-trend") {
                data = await reportingApi.getDepartmentVolumeTrend(deptId, start, end, bucket);
            } else if (type === "dept-sla") {
                data = await reportingApi.getDepartmentSlaSummary(deptId, start, end);
            } else {
                data = await reportingApi.getDepartmentsPerformanceReport(start, end);
            }
            setResult(data);
            onGenerated();
        } catch (e: any) {
            setError(e?.response?.data?.message ?? e?.message ?? "Report generation failed");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="space-y-4">
            <p className="text-[11px] text-slate-400">Results are saved to Report History automatically.</p>

            <div className="grid grid-cols-4 gap-3">
                {/* Report type */}
                <div>
                    <label className="text-[10px] font-medium text-slate-600 uppercase tracking-wide">Report Type</label>
                    <select className="input mt-1 bg-white text-xs w-full" value={type} onChange={(e) => setType(e.target.value as GenType)}>
                        <option value="global-trend">Global Volume Trend</option>
                        <option value="dept-trend">Dept Volume Trend</option>
                        <option value="dept-sla">Dept SLA Summary</option>
                        <option value="depts-perf">All Depts Performance</option>
                    </select>
                </div>

                {/* Department (conditional) */}
                <div className={needsDept ? "" : "opacity-40 pointer-events-none"}>
                    <label className="text-[10px] font-medium text-slate-600 uppercase tracking-wide">Department</label>
                    <select className="input mt-1 bg-white text-xs w-full" value={deptId} onChange={(e) => setDeptId(Number(e.target.value))} disabled={!needsDept}>
                        {departments.map((d) => (
                            <option key={d.departmentId} value={d.departmentId}>{d.departmentName}</option>
                        ))}
                    </select>
                </div>

                {/* Start date */}
                <div>
                    <label className="text-[10px] font-medium text-slate-600 uppercase tracking-wide">From</label>
                    <input type="date" className="input mt-1 text-xs w-full" value={start} onChange={(e) => setStart(e.target.value)} />
                </div>

                {/* End date */}
                <div>
                    <label className="text-[10px] font-medium text-slate-600 uppercase tracking-wide">To</label>
                    <input type="date" className="input mt-1 text-xs w-full" value={end} onChange={(e) => setEnd(e.target.value)} />
                </div>
            </div>

            {needsBucket && (
                <div className="flex items-center gap-3">
                    <label className="text-[10px] font-medium text-slate-600 uppercase tracking-wide">Bucket</label>
                    <div className="flex gap-2">
                        {(["DAILY", "MONTHLY"] as TrendBucket[]).map((b) => (
                            <button
                                key={b}
                                onClick={() => setBucket(b)}
                                className={`h-7 px-3 rounded-[6px] text-xs font-medium border transition-colors ${bucket === b ? "bg-[#1E6FD9] text-white border-[#1E6FD9]" : "bg-white text-slate-600 hover:bg-[#FAFCFF]"}`}
                                style={bucket !== b ? { borderColor: "var(--border)" } : {}}
                            >
                                {b}
                            </button>
                        ))}
                    </div>
                </div>
            )}

            {error && <div className="text-xs text-red-600 bg-red-50 rounded-[6px] px-3 py-2">{error}</div>}

            <button className="btn-primary h-9 px-5 text-xs" onClick={generate} disabled={loading}>
                {loading ? "Generating…" : "Generate Report"}
            </button>

            {/* Inline result preview */}
            {result && <GeneratedReportPreview report={result} />}
        </div>
    );
}

// ── Inline result preview after generation ────────────────────────────────────
function GeneratedReportPreview({ report }: { report: ReportResponseDto }) {
    const series = report.series as TrendPointDto[] | null;
    const isTrend = Array.isArray(series) && series.length > 0 && "incidentCount" in (series[0] ?? {});

    return (
        <div className="rounded-[10px] border p-4 space-y-4" style={{ borderColor: "var(--border)", background: "#FAFCFF" }}>
            <div className="flex items-center justify-between">
                <div className="text-xs font-semibold text-slate-800">
                    Report #{report.reportId} · {report.reportType.replace(/_/g, " ")}
                </div>
                <div className="text-[10px] text-slate-400">{fmtDate(report.startDate)} → {fmtDate(report.endDate)}</div>
            </div>

            <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                <StatPill label="Total Incidents" value={report.incidentCount} color="#3B82F6" />
                <StatPill label="Resolved" value={report.resolvedIncidentCount} color="#10B981" />
                <StatPill label="SLA Breaches" value={report.slaBreachedCount} color="#EF4444" />
                <StatPill label="SLA Compliance" value={`${report.slaComplianceRate?.toFixed(1)}%`}
                    color={report.slaComplianceRate >= 90 ? "#10B981" : report.slaComplianceRate >= 70 ? "#F59E0B" : "#EF4444"}
                    sub={`Avg ${report.averageResolutionTimeHours?.toFixed(1)}h resolution`}
                />
            </div>

            {isTrend && (
                <ChartCard title="Volume Trend" height={200}>
                    <TrendAreaChart series={series as TrendPointDto[]} />
                </ChartCard>
            )}
        </div>
    );
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function AdminReportsPage() {
    const [tab, setTab] = useState<Tab>("overview");
    const [generateOpen, setGenerateOpen] = useState(false);
    const [viewingReport, setViewingReport] = useState<ReportResponseDto | null>(null);

    // Overview state
    const [overviewStart, setOverviewStart] = useState(startOfMonth());
    const [overviewEnd, setOverviewEnd] = useState(today());
    const [pieByDept, setPieByDept] = useState<any>(null);
    const [pieByStatus, setPieByStatus] = useState<any>(null);
    const [pieBySeverity, setPieBySeverity] = useState<any>(null);
    const [pieByCategory, setPieByCategory] = useState<any>(null);
    const [pieByBreach, setPieByBreach] = useState<any>(null);
    const [chartsLoading, setChartsLoading] = useState(false);
    const [chartsErr, setChartsErr] = useState<string | null>(null);

    // Trends state
    const [trendStart, setTrendStart] = useState(startOfYear());
    const [trendEnd, setTrendEnd] = useState(today());
    const [trendBucket, setTrendBucket] = useState<TrendBucket>("DAILY");
    const [globalTrend, setGlobalTrend] = useState<ReportResponseDto | null>(null);
    const [trendLoading, setTrendLoading] = useState(false);
    const [trendErr, setTrendErr] = useState<string | null>(null);
    // Dept volume trend (per-department)
    const [deptTrendId, setDeptTrendId] = useState<number>(0);
    const [deptTrend, setDeptTrend] = useState<ReportResponseDto | null>(null);
    const [deptTrendLoading, setDeptTrendLoading] = useState(false);
    const [deptTrendErr, setDeptTrendErr] = useState<string | null>(null);

    // Departments state
    const [deptStart, setDeptStart] = useState(startOfMonth());
    const [deptEnd, setDeptEnd] = useState(today());
    const [deptPerf, setDeptPerf] = useState<DepartmentPerformanceDto[]>([]);
    const [deptLoading, setDeptLoading] = useState(false);
    const [deptErr, setDeptErr] = useState<string | null>(null);

    // History state
    const [history, setHistory] = useState<ReportResponseDto[]>([]);
    const [histLoading, setHistLoading] = useState(false);
    const [histErr, setHistErr] = useState<string | null>(null);
    const [histSearch, setHistSearch] = useState("");
    const [histTypeFilter, setHistTypeFilter] = useState<ReportType | "">("");

    // Shared data
    const [departments, setDepartments] = useState<DepartmentResponseDto[]>([]);

    // Load departments once
    useEffect(() => {
        departmentsApi.list().then(setDepartments).catch(() => { });
    }, []);

    // ── Load charts ───────────────────────────────────────────────────────────
    const loadCharts = useCallback(async (s: string, e: string) => {
        setChartsLoading(true);
        setChartsErr(null);
        try {
            const [d, st, sv, cat, br] = await Promise.all([
                reportingApi.getIncidentsByDepartment(s, e),
                reportingApi.getIncidentsByStatus(s, e),
                reportingApi.getIncidentsBySeverity(s, e),
                reportingApi.getIncidentsByCategory(s, e),
                reportingApi.getSlaBreachesByDepartment(s, e),
            ]);
            setPieByDept(d); setPieByStatus(st); setPieBySeverity(sv);
            setPieByCategory(cat); setPieByBreach(br);
        } catch (err: any) {
            setChartsErr(err?.response?.data?.message ?? "Failed to load charts");
        } finally {
            setChartsLoading(false);
        }
    }, []);

    useEffect(() => { if (tab === "overview") loadCharts(overviewStart, overviewEnd); }, [tab]);

    // ── Load global trend ─────────────────────────────────────────────────────
    const loadTrend = useCallback(async (s: string, e: string, b: TrendBucket) => {
        setTrendLoading(true);
        setTrendErr(null);
        try {
            const data = await reportingApi.getGlobalVolumeTrend(s, e, b);
            setGlobalTrend(data);
        } catch (err: any) {
            setTrendErr(err?.response?.data?.message ?? "Failed to load trend data");
        } finally {
            setTrendLoading(false);
        }
    }, []);

    useEffect(() => { if (tab === "trends") loadTrend(trendStart, trendEnd, trendBucket); }, [tab]);

    const loadDeptTrend = useCallback(async (deptId: number, s: string, e: string, b: TrendBucket) => {
        if (!deptId) return;
        setDeptTrendLoading(true);
        setDeptTrendErr(null);
        try {
            const data = await reportingApi.getDepartmentVolumeTrend(deptId, s, e, b);
            setDeptTrend(data);
        } catch (err: any) {
            setDeptTrendErr(err?.response?.data?.message ?? "Failed to load department trend");
        } finally {
            setDeptTrendLoading(false);
        }
    }, []);

    // Load dept trend when departments are ready
    useEffect(() => {
        if (tab === "trends" && departments.length > 0 && deptTrendId === 0) {
            const firstId = departments[0].departmentId ?? 0;
            setDeptTrendId(firstId);
            loadDeptTrend(firstId, trendStart, trendEnd, trendBucket);
        }
    }, [tab, departments]);

    // ── Load dept performance ─────────────────────────────────────────────────
    const loadDeptPerf = useCallback(async (s: string, e: string) => {
        setDeptLoading(true);
        setDeptErr(null);
        try {
            const data = await reportingApi.getDepartmentsPerformance(s, e);
            setDeptPerf(data);
        } catch (err: any) {
            setDeptErr(err?.response?.data?.message ?? "Failed to load department data");
        } finally {
            setDeptLoading(false);
        }
    }, []);

    useEffect(() => { if (tab === "departments") loadDeptPerf(deptStart, deptEnd); }, [tab]);

    // ── Load history ──────────────────────────────────────────────────────────
    const loadHistory = useCallback(async () => {
        setHistLoading(true);
        setHistErr(null);
        try {
            setHistory(await reportingApi.getReportHistory());
        } catch (err: any) {
            setHistErr(err?.response?.data?.message ?? "Failed to load history");
        } finally {
            setHistLoading(false);
        }
    }, []);

    useEffect(() => { if (tab === "history") loadHistory(); }, [tab]);

    const filteredHistory = history.filter((r) => {
        if (histTypeFilter && r.reportType !== histTypeFilter) return false;
        if (histSearch) {
            const q = histSearch.toLowerCase();
            if (
                !String(r.reportId).includes(q) &&
                !r.reportType.toLowerCase().includes(q) &&
                !r.scope.toLowerCase().includes(q)
            ) return false;
        }
        return true;
    });

    // ── Tab bar ───────────────────────────────────────────────────────────────
    const TABS: { id: Tab; label: string }[] = [
        { id: "overview", label: "Overview" },
        { id: "trends", label: "Trends" },
        { id: "departments", label: "Departments" },
        { id: "history", label: "Report History" },
    ];

    return (
        <div className="flex flex-col h-full gap-3">
            {/* Page header */}
            <div className="flex items-center justify-between flex-wrap gap-2">
                <div>
                    <h1 className="text-base font-semibold text-slate-900">Reports & Analytics</h1>
                    <p className="text-xs text-slate-500 mt-0.5">Generate reports and view system-wide analytics</p>
                </div>
                <button
                    className="btn-primary h-8 px-4 text-xs flex items-center gap-1.5"
                    onClick={() => setGenerateOpen(true)}
                >
                    <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" />
                    </svg>
                    Generate Report
                </button>
            </div>

            {/* Generate Report Modal */}
            {generateOpen && (
                <>
                    <div className="fixed inset-0 bg-black/30 z-40" onClick={() => setGenerateOpen(false)} />
                    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                        <div className="bg-white rounded-[14px] shadow-2xl w-full max-w-xl overflow-hidden">
                            <div className="flex items-center justify-between px-5 py-4 border-b" style={{ borderColor: "var(--border)" }}>
                                <div className="text-sm font-semibold text-slate-900">Generate New Report</div>
                                <button onClick={() => setGenerateOpen(false)} className="p-1.5 rounded-[6px] hover:bg-slate-100 text-slate-400 hover:text-slate-700 transition-colors">
                                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}><path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                                </button>
                            </div>
                            <div className="p-5">
                                <GeneratePanel departments={departments} onGenerated={() => { loadHistory(); setGenerateOpen(false); setTab("history"); }} />
                            </div>
                        </div>
                    </div>
                </>
            )}

            {/* Report Detail Drawer */}
            {viewingReport && (
                <ReportDetailDrawer report={viewingReport} onClose={() => setViewingReport(null)} />
            )}

            {/* Tab bar */}
            <div className="flex gap-1 border-b" style={{ borderColor: "var(--border)" }}>
                {TABS.map((t) => (
                    <button
                        key={t.id}
                        onClick={() => setTab(t.id)}
                        className={`px-4 py-2 text-xs font-medium border-b-2 transition-colors -mb-px ${tab === t.id
                            ? "border-[#1E6FD9] text-[#1E6FD9]"
                            : "border-transparent text-slate-500 hover:text-slate-800"
                            }`}
                    >
                        {t.label}
                    </button>
                ))}
            </div>

            {/* ── OVERVIEW TAB ── */}
            {tab === "overview" && (
                <div className="flex flex-col gap-3 flex-1 overflow-y-auto">
                    {/* Date toolbar */}
                    <div className="card p-2 flex items-center gap-2 flex-nowrap">
                        <span className="text-[10px] text-slate-500 uppercase tracking-wide font-medium shrink-0">Period</span>
                        <input type="date" className="input h-8 text-xs min-w-0" value={overviewStart} onChange={(e) => setOverviewStart(e.target.value)} />
                        <span className="text-slate-400 text-xs shrink-0">→</span>
                        <input type="date" className="input h-8 text-xs min-w-0" value={overviewEnd} onChange={(e) => setOverviewEnd(e.target.value)} />
                        <button className="btn-primary h-8 px-4 text-xs shrink-0" onClick={() => loadCharts(overviewStart, overviewEnd)}>
                            Apply
                        </button>
                    </div>

                    {chartsErr && <div className="card p-3 text-xs text-red-600">{chartsErr}</div>}

                    {/* Pie charts grid */}
                    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
                        <ChartCard title="Incidents by Department" loading={chartsLoading} empty={!pieByDept || pieByDept?.data?.length === 0}>
                            {pieByDept && <PieChartWidget data={pieByDept} donut />}
                        </ChartCard>
                        <ChartCard title="Incidents by Status" loading={chartsLoading} empty={!pieByStatus || pieByStatus?.data?.length === 0}>
                            {pieByStatus && <PieChartWidget data={pieByStatus} />}
                        </ChartCard>
                        <ChartCard title="Incidents by Severity" loading={chartsLoading} empty={!pieBySeverity || pieBySeverity?.data?.length === 0}>
                            {pieBySeverity && <PieChartWidget data={pieBySeverity} donut />}
                        </ChartCard>
                        <ChartCard title="Incidents by Category" loading={chartsLoading} empty={!pieByCategory || pieByCategory?.data?.length === 0}>
                            {pieByCategory && <PieChartWidget data={pieByCategory} />}
                        </ChartCard>
                        <ChartCard title="SLA Breaches by Department" loading={chartsLoading} empty={!pieByBreach || pieByBreach?.data?.length === 0}>
                            {pieByBreach && <PieChartWidget data={pieByBreach} donut />}
                        </ChartCard>
                    </div>
                </div>
            )}

            {/* ── TRENDS TAB ── */}
            {tab === "trends" && (
                <div className="flex flex-col gap-3 flex-1 overflow-y-auto">
                    {/* Toolbar */}
                    <div className="card p-2 flex items-center gap-2 flex-nowrap">
                        <span className="text-[10px] text-slate-500 uppercase tracking-wide font-medium shrink-0">Period</span>
                        <input type="date" className="input h-8 text-xs min-w-0" value={trendStart} onChange={(e) => setTrendStart(e.target.value)} />
                        <span className="text-slate-400 text-xs shrink-0">→</span>
                        <input type="date" className="input h-8 text-xs min-w-0" value={trendEnd} onChange={(e) => setTrendEnd(e.target.value)} />
                        <div className="flex gap-1 shrink-0">
                            {(["DAILY", "MONTHLY"] as TrendBucket[]).map((b) => (
                                <button
                                    key={b}
                                    onClick={() => setTrendBucket(b)}
                                    className={`h-8 px-3 rounded-[6px] text-xs font-medium border transition-colors ${trendBucket === b ? "bg-[#1E6FD9] text-white border-[#1E6FD9]" : "bg-white text-slate-600 hover:bg-[#FAFCFF]"}`}
                                    style={trendBucket !== b ? { borderColor: "var(--border)" } : {}}
                                >
                                    {b}
                                </button>
                            ))}
                        </div>
                        <button className="btn-primary h-8 px-4 text-xs shrink-0" onClick={() => loadTrend(trendStart, trendEnd, trendBucket)}>
                            Apply
                        </button>
                    </div>

                    {/* Stats row */}
                    {globalTrend && (
                        <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                            <StatPill label="Total Incidents" value={globalTrend.incidentCount} color="#3B82F6" />
                            <StatPill label="Resolved" value={globalTrend.resolvedIncidentCount} color="#10B981" />
                            <StatPill label="SLA Breaches" value={globalTrend.slaBreachedCount} color="#EF4444" />
                            <StatPill label="SLA Compliance"
                                value={`${globalTrend.slaComplianceRate?.toFixed(1)}%`}
                                color={globalTrend.slaComplianceRate >= 90 ? "#10B981" : globalTrend.slaComplianceRate >= 70 ? "#F59E0B" : "#EF4444"}
                                sub={`Avg ${globalTrend.averageResolutionTimeHours?.toFixed(1)}h resolution`}
                            />
                        </div>
                    )}

                    <ChartCard
                        title="Global Incident Volume Trend"
                        subtitle={`${trendBucket.toLowerCase()} buckets`}
                        loading={trendLoading}
                        error={trendErr}
                        empty={!globalTrend || !Array.isArray(globalTrend.series) || (globalTrend.series as any[]).length === 0}
                        height={320}
                    >
                        {globalTrend?.series && <TrendAreaChart series={globalTrend.series as any} />}
                    </ChartCard>

                    {/* ── Department Volume Trend ── */}
                    <div className="card p-4 space-y-3">
                        <div className="flex flex-wrap items-center gap-2">
                            <div className="text-xs font-semibold text-slate-800 flex-1">Incident Volume Trend by Department</div>
                            <select
                                className="input h-7 text-xs bg-white"
                                value={deptTrendId}
                                onChange={(e) => {
                                    const id = Number(e.target.value);
                                    setDeptTrendId(id);
                                    loadDeptTrend(id, trendStart, trendEnd, trendBucket);
                                }}
                            >
                                {departments.map((d) => (
                                    <option key={d.departmentId} value={d.departmentId}>{d.departmentName}</option>
                                ))}
                            </select>
                            <button
                                className="btn-primary h-7 px-3 text-xs"
                                onClick={() => loadDeptTrend(deptTrendId, trendStart, trendEnd, trendBucket)}
                            >
                                Load
                            </button>
                        </div>

                        {deptTrend && (
                            <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                                <StatPill label="Total Incidents" value={deptTrend.incidentCount} color="#3B82F6" />
                                <StatPill label="Resolved" value={deptTrend.resolvedIncidentCount} color="#10B981" />
                                <StatPill label="SLA Breaches" value={deptTrend.slaBreachedCount} color="#EF4444" />
                                <StatPill
                                    label="SLA Compliance"
                                    value={`${deptTrend.slaComplianceRate?.toFixed(1)}%`}
                                    color={deptTrend.slaComplianceRate >= 90 ? "#10B981" : deptTrend.slaComplianceRate >= 70 ? "#F59E0B" : "#EF4444"}
                                />
                            </div>
                        )}

                        <ChartCard
                            title=""
                            loading={deptTrendLoading}
                            error={deptTrendErr}
                            empty={!deptTrend || !Array.isArray(deptTrend.series) || (deptTrend.series as any[]).length === 0}
                            height={260}
                        >
                            {deptTrend?.series && <TrendAreaChart series={deptTrend.series as any} />}
                        </ChartCard>
                    </div>
                </div>
            )}

            {/* ── DEPARTMENTS TAB ── */}
            {tab === "departments" && (
                <div className="flex flex-col gap-3 flex-1 overflow-y-auto">
                    {/* Toolbar */}
                    <div className="card p-2 flex items-center gap-2 flex-nowrap">
                        <span className="text-[10px] text-slate-500 uppercase tracking-wide font-medium shrink-0">Period</span>
                        <input type="date" className="input h-8 text-xs min-w-0" value={deptStart} onChange={(e) => setDeptStart(e.target.value)} />
                        <span className="text-slate-400 text-xs shrink-0">→</span>
                        <input type="date" className="input h-8 text-xs min-w-0" value={deptEnd} onChange={(e) => setDeptEnd(e.target.value)} />
                        <button className="btn-primary h-8 px-4 text-xs shrink-0" onClick={() => loadDeptPerf(deptStart, deptEnd)}>
                            Apply
                        </button>
                    </div>

                    {deptErr && <div className="card p-3 text-xs text-red-600">{deptErr}</div>}

                    {/* Bar chart */}
                    <ChartCard
                        title="Department Performance Comparison"
                        subtitle="incidents · resolved · breaches"
                        loading={deptLoading}
                        empty={deptPerf.length === 0}
                        height={300}
                    >
                        <DeptPerformanceBarChart data={deptPerf} />
                    </ChartCard>

                    {/* SLA compliance bars */}
                    {!deptLoading && deptPerf.length > 0 && (
                        <div className="card p-4 space-y-4">
                            <div className="text-xs font-semibold text-slate-800">SLA Compliance by Department</div>
                            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                                {deptPerf.map((d) => (
                                    <SlaComplianceBar key={d.departmentId} rate={d.slaComplianceRate} label={d.departmentName} />
                                ))}
                            </div>
                        </div>
                    )}

                    {/* Table */}
                    {!deptLoading && deptPerf.length > 0 && (
                        <div className="card overflow-x-auto">
                            <div className="px-4 py-3 border-b text-xs font-semibold text-slate-800" style={{ borderColor: "var(--border)" }}>
                                Detailed Table
                            </div>
                            <table className="w-full text-xs border-collapse min-w-[600px]">
                                <thead>
                                    <tr style={{ background: "#F8FAFD" }}>
                                        <th className="text-left px-3 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Department</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Resolved</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Unresolved</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Breaches</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">SLA %</th>
                                        <th className="text-right px-2 py-2 text-slate-500 font-medium uppercase tracking-wide text-[10px]">Avg Res (h)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {deptPerf.map((d, i) => (
                                        <tr key={d.departmentId} className="border-t" style={{ borderColor: "var(--border)", background: i % 2 === 0 ? "white" : "#FAFCFF" }}>
                                            <td className="px-3 py-2 font-medium text-slate-800">{d.departmentName}</td>
                                            <td className="px-2 py-2 text-right text-green-600">{d.resolvedIncidentCount}</td>
                                            <td className="px-2 py-2 text-right text-slate-700">{Math.max(0, d.incidentCount - d.resolvedIncidentCount - d.slaBreachedCount)}</td>
                                            <td className="px-2 py-2 text-right text-red-500">{d.slaBreachedCount}</td>
                                            <td className="px-2 py-2 text-right">
                                                <span className={`font-semibold ${d.slaComplianceRate >= 90 ? "text-green-600" : d.slaComplianceRate >= 70 ? "text-amber-600" : "text-red-500"}`}>
                                                    {d.slaComplianceRate?.toFixed(1)}%
                                                </span>
                                            </td>
                                            <td className="px-2 py-2 text-right text-slate-600">{d.averageResolutionTimeHours?.toFixed(1)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {/* ── HISTORY TAB ── */}
            {tab === "history" && (
                <div className="flex flex-col gap-2 flex-1 min-h-0">
                    {/* Toolbar — search + filters all in one row */}
                    <div className="card p-2 flex items-center gap-2 flex-nowrap overflow-x-auto">
                        <div className="relative shrink-0 w-44">
                            <svg className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-slate-400 pointer-events-none" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-4.35-4.35M16.65 16.65A7.5 7.5 0 1 0 4.5 4.5a7.5 7.5 0 0 0 12.15 12.15z" />
                            </svg>
                            <input className="input pl-8 h-8 text-xs w-full" placeholder="Search…" value={histSearch} onChange={(e) => setHistSearch(e.target.value)} />
                        </div>
                        {/* Filters inline */}
                        <select className="input h-8 text-xs bg-white shrink-0" value={histTypeFilter} onChange={(e) => setHistTypeFilter(e.target.value as ReportType | "")}>
                            <option value="">All Types</option>
                            <option value="VOLUME_TREND">Volume Trend</option>
                            <option value="SLA_COMPLIANCE">SLA Compliance</option>
                            <option value="DEPARTMENT_PERFORMANCE">Dept Performance</option>
                            <option value="PERIOD_REPORT">Period Report</option>
                            <option value="MONTHLY_DISTRIBUTION">Monthly Distribution</option>
                        </select>
                        <span className="text-[10px] text-slate-400 shrink-0">{filteredHistory.length} result{filteredHistory.length !== 1 ? "s" : ""}</span>
                        <button className="h-8 px-3 text-xs rounded-[8px] border font-medium text-slate-600 hover:bg-slate-50 transition-colors ml-auto shrink-0" style={{ borderColor: "var(--border)" }} onClick={loadHistory}>Refresh</button>
                    </div>

                    <div className="card flex-1 min-h-0 overflow-y-auto">
                        {histLoading && <div className="p-6 text-xs text-slate-400 text-center">Loading…</div>}
                        {histErr && <div className="p-3 text-xs text-red-600">{histErr}</div>}
                        {!histLoading && !histErr && (
                            <ReportHistoryTable
                                reports={filteredHistory}
                                onView={setViewingReport}
                            />
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
