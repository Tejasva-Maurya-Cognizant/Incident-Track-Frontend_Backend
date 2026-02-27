import {
    PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
    BarChart, Bar, XAxis, YAxis, CartesianGrid,
    Area, AreaChart,
} from "recharts";
import type { PieChartResponseDto, DepartmentPerformanceDto, TrendPointDto } from "../../features/reporting/types";

// ── Palette ───────────────────────────────────────────────────────────────────
export const PALETTE = [
    "#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6",
    "#06B6D4", "#F97316", "#84CC16", "#EC4899", "#6366F1",
];

// ── Shared card wrapper ───────────────────────────────────────────────────────
interface ChartCardProps {
    title: string;
    subtitle?: string;
    loading?: boolean;
    error?: string | null;
    empty?: boolean;
    children: React.ReactNode;
    height?: number;
}

export function ChartCard({ title, subtitle, loading, error, empty, children, height = 280 }: ChartCardProps) {
    return (
        <div className="card p-4 flex flex-col gap-3">
            <div>
                <div className="text-xs font-semibold text-slate-800">{title}</div>
                {subtitle && <div className="text-[10px] text-slate-400 mt-0.5">{subtitle}</div>}
            </div>
            <div style={{ height }} className="w-full flex items-center justify-center">
                {loading && <span className="text-xs text-slate-400">Loading…</span>}
                {!loading && error && <span className="text-xs text-red-500">{error}</span>}
                {!loading && !error && empty && <span className="text-xs text-slate-400">No data for this period.</span>}
                {!loading && !error && !empty && children}
            </div>
        </div>
    );
}

// ── Pie / Donut chart ─────────────────────────────────────────────────────────
interface PieProps {
    data: PieChartResponseDto;
    donut?: boolean;
}

export function PieChartWidget({ data, donut = false }: PieProps) {
    const total = data.data.reduce((s, d) => s + d.value, 0);

    const renderLabel = ({ cx, cy, midAngle, innerRadius, outerRadius, percent }: any) => {
        if (percent < 0.05) return null;
        const RADIAN = Math.PI / 180;
        const radius = innerRadius + (outerRadius - innerRadius) * 0.6;
        const x = cx + radius * Math.cos(-midAngle * RADIAN);
        const y = cy + radius * Math.sin(-midAngle * RADIAN);
        return (
            <text x={x} y={y} fill="white" textAnchor="middle" dominantBaseline="central" fontSize={10} fontWeight={600}>
                {`${(percent * 100).toFixed(0)}%`}
            </text>
        );
    };

    return (
        <ResponsiveContainer width="100%" height="100%">
            <PieChart>
                <Pie
                    data={data.data}
                    dataKey="value"
                    nameKey="label"
                    cx="50%"
                    cy="50%"
                    innerRadius={donut ? "45%" : 0}
                    outerRadius="70%"
                    labelLine={false}
                    label={renderLabel}
                >
                    {data.data.map((_, i) => (
                        <Cell key={i} fill={PALETTE[i % PALETTE.length]} />
                    ))}
                </Pie>
                <Tooltip
                    formatter={(val: number | undefined, name: string | undefined) => [`${(val ?? 0)} (${total > 0 ? (((val ?? 0) / total) * 100).toFixed(1) : 0}%)`, name ?? ""]}
                    contentStyle={{ fontSize: 11, borderRadius: 8, border: "1px solid #E2E8F0" }}
                />
                <Legend
                    iconType="circle"
                    iconSize={8}
                    formatter={(val) => <span style={{ fontSize: 11, color: "#475569" }}>{val}</span>}
                />
            </PieChart>
        </ResponsiveContainer>
    );
}

// ── Trend line/area chart ─────────────────────────────────────────────────────
interface TrendProps {
    series: TrendPointDto[];
    showBreaches?: boolean;
}

export function TrendAreaChart({ series, showBreaches = true }: TrendProps) {
    return (
        <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={series} margin={{ top: 4, right: 8, bottom: 4, left: 0 }}>
                <defs>
                    <linearGradient id="gradIncident" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#3B82F6" stopOpacity={0.25} />
                        <stop offset="95%" stopColor="#3B82F6" stopOpacity={0} />
                    </linearGradient>
                    <linearGradient id="gradBreach" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#EF4444" stopOpacity={0.2} />
                        <stop offset="95%" stopColor="#EF4444" stopOpacity={0} />
                    </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" />
                <XAxis dataKey="label" tick={{ fontSize: 10, fill: "#94A3B8" }} tickLine={false} axisLine={false} />
                <YAxis tick={{ fontSize: 10, fill: "#94A3B8" }} tickLine={false} axisLine={false} allowDecimals={false} width={28} />
                <Tooltip contentStyle={{ fontSize: 11, borderRadius: 8, border: "1px solid #E2E8F0" }} />
                <Legend iconType="circle" iconSize={8} formatter={(val) => <span style={{ fontSize: 11, color: "#475569" }}>{val}</span>} />
                <Area type="monotone" dataKey="incidentCount" name="Incidents" stroke="#3B82F6" strokeWidth={2} fill="url(#gradIncident)" dot={false} />
                {showBreaches && (
                    <Area type="monotone" dataKey="slaBreachedCount" name="SLA Breaches" stroke="#EF4444" strokeWidth={2} fill="url(#gradBreach)" dot={false} />
                )}
            </AreaChart>
        </ResponsiveContainer>
    );
}

// ── Department performance bar chart ─────────────────────────────────────────
interface DeptBarProps {
    data: DepartmentPerformanceDto[];
}

export function DeptPerformanceBarChart({ data }: DeptBarProps) {
    const chartData = data.map((d) => ({
        name: d.departmentName.length > 10 ? d.departmentName.slice(0, 10) + "…" : d.departmentName,
        fullName: d.departmentName,
        Resolved: d.resolvedIncidentCount,
        Unresolved: Math.max(0, d.incidentCount - d.resolvedIncidentCount - d.slaBreachedCount),
        Breached: d.slaBreachedCount,
    }));

    return (
        <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 4, right: 8, bottom: 4, left: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#F1F5F9" vertical={false} />
                <XAxis dataKey="name" tick={{ fontSize: 10, fill: "#94A3B8" }} tickLine={false} axisLine={false} />
                <YAxis tick={{ fontSize: 10, fill: "#94A3B8" }} tickLine={false} axisLine={false} allowDecimals={false} width={28} />
                <Tooltip
                    formatter={(val, name) => [val, name]}
                    labelFormatter={(label, payload) => payload?.[0]?.payload?.fullName ?? label}
                    contentStyle={{ fontSize: 11, borderRadius: 8, border: "1px solid #E2E8F0" }}
                />
                <Legend iconType="circle" iconSize={8} formatter={(val) => <span style={{ fontSize: 11, color: "#475569" }}>{val}</span>} />
                <Bar dataKey="Resolved" stackId="stack" fill="#10B981" maxBarSize={32} />
                <Bar dataKey="Unresolved" stackId="stack" fill="#3B82F6" maxBarSize={32} />
                <Bar dataKey="Breached" stackId="stack" fill="#EF4444" maxBarSize={32} radius={[3, 3, 0, 0]} />
            </BarChart>
        </ResponsiveContainer>
    );
}

// ── SLA compliance horizontal bar ─────────────────────────────────────────────
interface SlaBarProps {
    rate: number;          // 0-100
    label?: string;
}

export function SlaComplianceBar({ rate, label }: SlaBarProps) {
    const color = rate >= 90 ? "#10B981" : rate >= 70 ? "#F59E0B" : "#EF4444";
    return (
        <div className="space-y-1">
            {label && <div className="text-xs text-slate-500 truncate">{label}</div>}
            <div className="flex items-center gap-2">
                <div className="flex-1 h-2 rounded-full bg-slate-100 overflow-hidden">
                    <div
                        className="h-2 rounded-full transition-all"
                        style={{ width: `${Math.min(rate, 100)}%`, background: color }}
                    />
                </div>
                <span className="text-xs font-semibold tabular-nums" style={{ color, minWidth: 42, textAlign: "right" }}>
                    {rate.toFixed(1)}%
                </span>
            </div>
        </div>
    );
}

// ── Stat pill ─────────────────────────────────────────────────────────────────
interface StatPillProps {
    label: string;
    value: number | string;
    color?: string;
    sub?: string;
}
export function StatPill({ label, value, color = "#3B82F6", sub }: StatPillProps) {
    return (
        <div className="rounded-[10px] border bg-white px-4 py-3 flex flex-col gap-0.5" style={{ borderColor: "var(--border)" }}>
            <div className="text-[10px] uppercase tracking-wide text-slate-400 font-medium">{label}</div>
            <div className="text-xl font-bold tabular-nums" style={{ color }}>{value}</div>
            {sub && <div className="text-[10px] text-slate-400">{sub}</div>}
        </div>
    );
}
