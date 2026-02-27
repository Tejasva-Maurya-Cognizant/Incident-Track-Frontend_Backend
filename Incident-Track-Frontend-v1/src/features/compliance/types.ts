import type { LocalDateTime } from "../../types/common";

export type ActionType =
    | "INCIDENT_CREATED"
    | "INCIDENT_UPDATED"
    | "INCIDENT_STATUS_CHANGED"
    | "INCIDENT_WITHDRAWN"
    | "TASK_CREATED"
    | "TASK_ASSIGNED"
    | "TASK_STATUS_CHANGED"
    | "CATEGORY_CHANGED"
    | "NOTE_ADDED";

export type BreachStatus = "OPEN" | "RESOLVED";

export interface AuditLogResponseDto {
    logId: number;
    incidentId: number | null;
    userId: number | null;
    username: string | null;
    actionType: ActionType;
    timestamp: LocalDateTime;
    details: string | null;
}

export interface SlaBreachResponseDto {
    breachId: number;
    incidentId: number;
    incidentStatus: string;
    slaDueAt: LocalDateTime;
    breachedAt: LocalDateTime;
    breachMinutes: number;
    breachStatus: BreachStatus;
    reason: string | null;
}
