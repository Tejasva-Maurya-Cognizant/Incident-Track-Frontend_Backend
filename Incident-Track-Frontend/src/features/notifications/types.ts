export type NotificationType =
    | "INCIDENT_REPORTED"
    | "CRITICAL_INCIDENT_ALERT"
    | "INCIDENT_WITHDRAWN"
    | "TASK_ASSIGNED"
    | "INCIDENT_RESOLVED"
    | "SLA_BREACHED";

export type NotificationStatus = "READ" | "UNREAD";

export interface NotificationResponseDto {
    notificationId: number;
    userId: number;
    type: NotificationType;
    message: string;
    status: NotificationStatus;
    createdDateTime: string; // ISO LocalDateTime
}
