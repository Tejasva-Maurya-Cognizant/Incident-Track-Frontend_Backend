import { useCallback, useEffect, useState } from "react";
import { reportingApi } from "../../features/reporting/api";
import type { PieChartResponseDto } from "../../features/reporting/types";
import { ChartCard, PieChartWidget, SlaComplianceBar, StatPill } from "../../components/charts/ReportCharts";

// ── helpers ───────────────────────────────────────────────────────────────────
const today = () => new Date().toISOString().slice(0, 10);
const startOfMonth = () => {
    const d = new Date(); d.setDate(1);
    return d.toISOString().slice(0, 10);
};

interface ChartState {
    data: PieChartResponseDto | null;
    loading: boolean;
    error: string | null;
}
const emptyChart = (): ChartState => ({ data: null, loading: false, error: null });

export default function ManagerChartsPage() {
    const [start, setStart] = useState(startOfMonth());
    const [end, setEnd] = useState(today());

    const [byDept, setByDept] = useState<ChartState>(emptyChart());
    const [byStatus, setByStatus] = useState<ChartState>(emptyChart());
    const [bySeverity, setBySeverity] = useState<ChartState>(emptyChart());
    const [byCategory, setByCategory] = useState<ChartState>(emptyChart());
    const [byBreach, setByBreach] = useState<ChartState>(emptyChart());

    // Summary stats derived from byDept + byStatus
    const totalIncidents = byDept.data?.data.reduce((s, d) => s + d.value, 0) ?? null;
    const breachTotal = byBreach.data?.data.reduce((s, d) => s + d.value, 0) ?? null;
    const slaCompliance = (totalIncidents != null && breachTotal != null && totalIncidents > 0)
        ? ((totalIncidents - breachTotal) / totalIncidents) * 100
        : null;

    const load = useCallback(async (s: string, e: string) => {
        const sets = [setByDept, setByStatus, setBySeverity, setByCategory, setByBreach];
        sets.forEach((set) => set({ data: null, loading: true, error: null }));

        const calls = [
            reportingApi.getIncidentsByDepartment(s, e),
            reportingApi.getIncidentsByStatus(s, e),
            reportingApi.getIncidentsBySeverity(s, e),
            reportingApi.getIncidentsByCategory(s, e),
            reportingApi.getSlaBreachesByDepartment(s, e),
        ];

        const results = await Promise.allSettled(calls);

        const setters = [setByDept, setByStatus, setBySeverity, setByCategory, setByBreach];
        results.forEach((r, i) => {
            if (r.status === "fulfilled") {
                setters[i]({ data: r.value, loading: false, error: null });
            } else {
                const msg = (r.reason as any)?.response?.data?.message ?? "Failed to load";
                setters[i]({ data: null, loading: false, error: msg });
            }
        });
    }, []);

    useEffect(() => { load(start, end); }, []);

    return (
        <div className="flex flex-col h-full gap-3">
            {/* Header */}
            <div className="flex items-center justify-between flex-wrap gap-2">
                <div>
                    <h1 className="text-base font-semibold text-slate-900">Analytics Dashboard</h1>
                    <p className="text-xs text-slate-500 mt-0.5">Incident distribution charts for the selected period</p>
                </div>
            </div>

            {/* Date toolbar */}
            <div className="card p-2 flex flex-wrap items-center gap-2">
                <span className="text-[10px] text-slate-500 uppercase tracking-wide font-medium">Period</span>
                <input type="date" className="input h-8 text-xs" value={start} onChange={(e) => setStart(e.target.value)} />
                <span className="text-slate-400 text-xs">→</span>
                <input type="date" className="input h-8 text-xs" value={end} onChange={(e) => setEnd(e.target.value)} />
                <button className="btn-primary h-8 px-4 text-xs" onClick={() => load(start, end)}>
                    Apply
                </button>
            </div>

            {/* Summary stat pills */}
            {totalIncidents !== null && (
                <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
                    <StatPill label="Total Incidents" value={totalIncidents} color="#3B82F6" />
                    <StatPill label="SLA Breaches" value={breachTotal ?? 0} color="#EF4444" />
                    {slaCompliance !== null && (
                        <StatPill
                            label="SLA Compliance"
                            value={`${slaCompliance.toFixed(1)}%`}
                            color={slaCompliance >= 90 ? "#10B981" : slaCompliance >= 70 ? "#F59E0B" : "#EF4444"}
                        />
                    )}
                    <StatPill
                        label="Departments"
                        value={byDept.data?.data.length ?? 0}
                        color="#8B5CF6"
                        sub="with incidents"
                    />
                </div>
            )}

            {/* Charts grid — 2 cols on medium+ */}
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 flex-1">
                <ChartCard
                    title="Incidents by Department"
                    subtitle="distribution across departments"
                    loading={byDept.loading}
                    error={byDept.error}
                    empty={!byDept.data || byDept.data.data.length === 0}
                >
                    {byDept.data && <PieChartWidget data={byDept.data} donut />}
                </ChartCard>

                <ChartCard
                    title="Incidents by Status"
                    subtitle="current state breakdown"
                    loading={byStatus.loading}
                    error={byStatus.error}
                    empty={!byStatus.data || byStatus.data.data.length === 0}
                >
                    {byStatus.data && <PieChartWidget data={byStatus.data} />}
                </ChartCard>

                <ChartCard
                    title="Incidents by Severity"
                    subtitle="CRITICAL · HIGH · MEDIUM · LOW"
                    loading={bySeverity.loading}
                    error={bySeverity.error}
                    empty={!bySeverity.data || bySeverity.data.data.length === 0}
                >
                    {bySeverity.data && <PieChartWidget data={bySeverity.data} donut />}
                </ChartCard>

                <ChartCard
                    title="Incidents by Category"
                    subtitle="top categories with most incidents"
                    loading={byCategory.loading}
                    error={byCategory.error}
                    empty={!byCategory.data || byCategory.data.data.length === 0}
                >
                    {byCategory.data && <PieChartWidget data={byCategory.data} />}
                </ChartCard>
            </div>

            {/* SLA breaches by department — full width + compliance bars */}
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <ChartCard
                    title="SLA Breaches by Department"
                    subtitle="departments with most SLA violations"
                    loading={byBreach.loading}
                    error={byBreach.error}
                    empty={!byBreach.data || byBreach.data.data.length === 0}
                >
                    {byBreach.data && <PieChartWidget data={byBreach.data} donut />}
                </ChartCard>

                {/* SLA compliance mini bars derived from dept + breach data */}
                {byDept.data && byBreach.data && byDept.data.data.length > 0 && (
                    <div className="card p-4 space-y-3">
                        <div className="text-xs font-semibold text-slate-800">SLA Compliance by Department</div>
                        <div className="space-y-2.5">
                            {byDept.data.data.map((dept) => {
                                const breached = byBreach.data!.data.find((b) => b.label === dept.label)?.value ?? 0;
                                const rate = dept.value > 0 ? ((dept.value - breached) / dept.value) * 100 : 100;
                                return <SlaComplianceBar key={dept.label} label={dept.label} rate={rate} />;
                            })}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
