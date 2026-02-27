import type { LocalDateTime } from "../../types/common";

export type TrendBucket = "DAILY" | "MONTHLY";
export type ReportType = "VOLUME_TREND" | "SLA_COMPLIANCE" | "DEPARTMENT_PERFORMANCE" | "PERIOD_REPORT" | "MONTHLY_DISTRIBUTION";
export type ReportScope = "GLOBAL" | "DEPARTMENT" | "CATEGORY" | "PERIOD";

export interface TrendPointDto {
    label: string;           // "2026-02-07" or "2026-02"
    incidentCount: number;
    slaBreachedCount: number;
}

export interface DepartmentPerformanceDto {
    departmentId: number;
    departmentName: string;
    incidentCount: number;
    resolvedIncidentCount: number;
    slaBreachedCount: number;
    slaComplianceRate: number;
    averageResolutionTimeHours: number;
}

export interface SlaSummaryDto {
    incidentCount: number;
    resolvedIncidentCount: number;
    slaBreachedCount: number;
    slaComplianceRate: number;
    openCount: number;
    inProgressCount: number;
    cancelledCount: number;
}

export interface PieSliceDto {
    label: string;
    value: number;
}

export interface PieChartResponseDto {
    title: string;
    data: PieSliceDto[];
}

export interface ReportResponseDto {
    reportId: number;
    reportType: ReportType;
    scope: ReportScope;
    scopeRefId: number | null;
    incidentCount: number;
    resolvedIncidentCount: number;
    slaBreachedCount: number;
    slaComplianceRate: number;
    averageResolutionTimeHours: number;
    startDate: string;   // "YYYY-MM-DD"
    endDate: string;
    series: TrendPointDto[] | DepartmentPerformanceDto[] | SlaSummaryDto | null;
    generatedAt: LocalDateTime;
}
