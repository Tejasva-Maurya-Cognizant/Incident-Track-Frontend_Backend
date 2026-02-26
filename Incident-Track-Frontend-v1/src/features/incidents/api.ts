import { api } from "../../lib/axios/axiosInstance";
import type { PagedResponse, PageParams } from "../../types/pagination";
import type {
  IncidentRequestDTO,
  IncidentResponseDTO,
  IncidentSeverity,
  IncidentStatus,
  IncidentStatusUpdateRequestDTO,
} from "./types";

const BASE = "/incidents";

export const incidentsApi = {
  // EMPLOYEE/MANAGER/ADMIN: create
  create: async (body: IncidentRequestDTO) => {
    const res = await api.post<IncidentResponseDTO>(`${BASE}`, body);
    return res.data;
  },

  // "My incidents" for current user (non-paged)
  listMine: async () => {
    const res = await api.get<IncidentResponseDTO[]>(`${BASE}`);
    return res.data;
  },

  // "My incidents" paged
  listMinePaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<IncidentResponseDTO>>(`${BASE}/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  // Admin/Manager: all incidents (non-paged)
  listAllAdminManager: async () => {
    const res = await api.get<IncidentResponseDTO[]>(`${BASE}/admin-manager/all`);
    return res.data;
  },

  // Admin/Manager: all incidents paged
  listAllAdminManagerPaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<IncidentResponseDTO>>(`${BASE}/admin-manager/all/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  // Filters — non-paged
  listByStatus: async (status: IncidentStatus) => {
    const res = await api.get<IncidentResponseDTO[]>(`${BASE}/status/${status}`);
    return res.data;
  },

  // Filters — paged
  listByStatusPaged: async (status: IncidentStatus, p: PageParams) => {
    const res = await api.get<PagedResponse<IncidentResponseDTO>>(`${BASE}/status/${status}/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  listBySeverity: async (sev: IncidentSeverity) => {
    const res = await api.get<IncidentResponseDTO[]>(`${BASE}/calculatedSeverity/${sev}`);
    return res.data;
  },

  listBySeverityPaged: async (sev: IncidentSeverity, p: PageParams) => {
    const res = await api.get<PagedResponse<IncidentResponseDTO>>(`${BASE}/calculatedSeverity/${sev}/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  listCritical: async () => {
    const res = await api.get<IncidentResponseDTO[]>(`${BASE}/CRITICAL`);
    return res.data;
  },

  listCriticalPaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<IncidentResponseDTO>>(`${BASE}/CRITICAL/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  // Detail (your backend has both paths; we'll pick based on role in UI)
  getByIdMine: async (incidentId: number) => {
    const res = await api.get<IncidentResponseDTO>(`${BASE}/${incidentId}`);
    return res.data;
  },

  getByIdAdminManager: async (incidentId: number) => {
    const res = await api.get<IncidentResponseDTO>(`${BASE}/admin-manager/${incidentId}`);
    return res.data;
  },

  // Withdraw (all roles)
  withdraw: async (incidentId: number) => {
    const res = await api.put<IncidentResponseDTO>(`${BASE}/withdraw/${incidentId}`);
    return res.data;
  },

  // Update status (ADMIN/MANAGER as per your controller)
  updateStatusAdminManager: async (incidentId: number, body: IncidentStatusUpdateRequestDTO) => {
    const res = await api.put<IncidentResponseDTO>(`${BASE}/${incidentId}/status`, body);
    return res.data;
  },
};