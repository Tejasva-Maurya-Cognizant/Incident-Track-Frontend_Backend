export type TaskStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED";

export interface TaskRequestDTO {
  title: string;
  description: string;
  assignedTo: number; // user id
  incidentId: number;
}

export interface TaskResponseDTO {
  taskId: number;
  title: string;
  description: string;
  status: TaskStatus;
  dueDate: string | null;      // LocalDateTime as ISO string
  createdDate: string | null;
  assignedTo: number;          // userId of assignee
  assignedBy: number;          // userId of assigner
  incidentId: number;
}

export interface TaskStatusUpdateRequestDTO {
  status: TaskStatus;
}
