export type IncidentStatus = "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CANCELLED";
export type IncidentSeverity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";

export interface IncidentRequestDTO {
  categoryId: number;
  description: string;
  isCritical?: boolean;
}

export interface IncidentResponseDTO {
  incidentId: number;
  categoryId: number;
  categoryName: string;
  subCategory?: string;
  departmentName?: string | null;
  description: string;

  userId: number;
  username: string;

  status: IncidentStatus;
  calculatedSeverity: IncidentSeverity;
  isCritical: boolean;

  reportedDate: string; // LocalDateTime as ISO string
  resolvedDate?: string | null;
  slaHours?: number | null;
}

export interface IncidentStatusUpdateRequestDTO {
  status: IncidentStatus;
  note?: string;
}
