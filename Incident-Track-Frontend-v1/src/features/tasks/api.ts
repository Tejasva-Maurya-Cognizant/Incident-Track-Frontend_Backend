import { api } from "../../lib/axios/axiosInstance";
import type { PagedResponse, PageParams } from "../../types/pagination";
import type {
  TaskRequestDTO,
  TaskResponseDTO,
  TaskStatusUpdateRequestDTO,
} from "./types";

const BASE = "/tasks";

export const tasksApi = {
  // ─── MANAGER only ────────────────────────────────────────────────────────────

  /** POST /api/tasks  (MANAGER) */
  create: async (body: TaskRequestDTO) => {
    const res = await api.post<TaskResponseDTO>(`${BASE}`, body);
    return res.data;
  },

  // ─── ADMIN / MANAGER ─────────────────────────────────────────────────────────

  /** GET /api/tasks/paged  (ADMIN | MANAGER) */
  listAllPaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<TaskResponseDTO>>(`${BASE}/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  /** GET /api/tasks/{taskId}  (ADMIN | MANAGER) */
  getById: async (taskId: number) => {
    const res = await api.get<TaskResponseDTO>(`${BASE}/${taskId}`);
    return res.data;
  },

  /** GET /api/tasks/incident/{incidentId}/paged  (ADMIN | MANAGER) */
  listByIncidentPaged: async (incidentId: number, p: PageParams) => {
    const res = await api.get<PagedResponse<TaskResponseDTO>>(
      `${BASE}/incident/${incidentId}/paged`,
      { params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir } }
    );
    return res.data;
  },

  /** GET /api/tasks/assignedTo/{userId}/paged  (ADMIN | MANAGER) */
  listByAssigneePaged: async (userId: number, p: PageParams) => {
    const res = await api.get<PagedResponse<TaskResponseDTO>>(
      `${BASE}/assignedTo/${userId}/paged`,
      { params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir } }
    );
    return res.data;
  },

  /** GET /api/tasks/assignedByMe/paged  (MANAGER) */
  listByMePaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<TaskResponseDTO>>(`${BASE}/assignedByMe/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  /** GET /api/tasks/status/{status}/paged  (ADMIN | MANAGER) */
  listByStatusPaged: async (status: string, p: PageParams) => {
    const res = await api.get<PagedResponse<TaskResponseDTO>>(
      `${BASE}/status/${status}/paged`,
      { params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir } }
    );
    return res.data;
  },

  // ─── EMPLOYEE ────────────────────────────────────────────────────────────────

  /** GET /api/tasks/assignedToMe/paged  (EMPLOYEE) */
  listAssignedToMePaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<TaskResponseDTO>>(`${BASE}/assignedToMe/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  // ─── EMPLOYEE / MANAGER ──────────────────────────────────────────────────────

  /** PATCH /api/tasks/{taskId}/status  (EMPLOYEE | MANAGER) */
  updateStatus: async (taskId: number, body: TaskStatusUpdateRequestDTO) => {
    const res = await api.patch<string>(`${BASE}/${taskId}/status`, body);
    return res.data;
  },
};
