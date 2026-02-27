import { api } from "../../lib/axios/axiosInstance";
import type {
    ReportResponseDto,
    DepartmentPerformanceDto,
    PieChartResponseDto,
    TrendBucket,
} from "./types";

const BASE = "/reports";

/** Shared date params helper */
function dateParams(start?: string, end?: string) {
    const p: Record<string, string> = {};
    if (start) p.start = start;
    if (end) p.end = end;
    return p;
}

export const reportingApi = {
    // ── ADMIN-only trend / performance endpoints ────────────────────────────

    /** GET /api/reports/global/volume-trend  (ADMIN) */
    getGlobalVolumeTrend: async (start?: string, end?: string, bucket: TrendBucket = "DAILY") => {
        const res = await api.get<ReportResponseDto>(`${BASE}/global/volume-trend`, {
            params: { ...dateParams(start, end), bucket },
        });
        return res.data;
    },

    /** GET /api/reports/department/volume-trend  (ADMIN) */
    getDepartmentVolumeTrend: async (departmentId: number, start?: string, end?: string, bucket: TrendBucket = "MONTHLY") => {
        const res = await api.get<ReportResponseDto>(`${BASE}/department/volume-trend`, {
            params: { departmentId, ...dateParams(start, end), bucket },
        });
        return res.data;
    },

    /** GET /api/reports/departments/performance  (ADMIN) */
    getDepartmentsPerformance: async (start?: string, end?: string) => {
        const res = await api.get<DepartmentPerformanceDto[]>(`${BASE}/departments/performance`, {
            params: dateParams(start, end),
        });
        return res.data;
    },

    /** GET /api/reports/department/sla-summary  (ADMIN) */
    getDepartmentSlaSummary: async (departmentId: number, start?: string, end?: string) => {
        const res = await api.get<ReportResponseDto>(`${BASE}/department/sla-summary`, {
            params: { departmentId, ...dateParams(start, end) },
        });
        return res.data;
    },

    /** GET /api/reports/departments/performance-report  (ADMIN) */
    getDepartmentsPerformanceReport: async (start?: string, end?: string) => {
        const res = await api.get<ReportResponseDto>(`${BASE}/departments/performance-report`, {
            params: dateParams(start, end),
        });
        return res.data;
    },

    /** GET /api/reports/history  (ADMIN) */
    getReportHistory: async () => {
        const res = await api.get<ReportResponseDto[]>(`${BASE}/history`);
        return res.data;
    },

    // ── ADMIN + MANAGER chart endpoints ────────────────────────────────────

    /** GET /api/reports/charts/incidents-by-department */
    getIncidentsByDepartment: async (start?: string, end?: string) => {
        const res = await api.get<PieChartResponseDto>(`${BASE}/charts/incidents-by-department`, {
            params: dateParams(start, end),
        });
        return res.data;
    },

    /** GET /api/reports/charts/incidents-by-category */
    getIncidentsByCategory: async (start?: string, end?: string) => {
        const res = await api.get<PieChartResponseDto>(`${BASE}/charts/incidents-by-category`, {
            params: dateParams(start, end),
        });
        return res.data;
    },

    /** GET /api/reports/charts/incidents-by-status */
    getIncidentsByStatus: async (start?: string, end?: string) => {
        const res = await api.get<PieChartResponseDto>(`${BASE}/charts/incidents-by-status`, {
            params: dateParams(start, end),
        });
        return res.data;
    },

    /** GET /api/reports/charts/sla-breaches-by-department */
    getSlaBreachesByDepartment: async (start?: string, end?: string) => {
        const res = await api.get<PieChartResponseDto>(`${BASE}/charts/sla-breaches-by-department`, {
            params: dateParams(start, end),
        });
        return res.data;
    },

    /** GET /api/reports/charts/incidents-by-severity */
    getIncidentsBySeverity: async (start?: string, end?: string) => {
        const res = await api.get<PieChartResponseDto>(`${BASE}/charts/incidents-by-severity`, {
            params: dateParams(start, end),
        });
        return res.data;
    },
};
