import { api } from "../../lib/axios/axiosInstance";
import type { PagedResponse, PageParams } from "../../types/pagination";
import type { DepartmentRequestDto, DepartmentResponseDto } from "./types";

const DEPT_BASE = "/departments";

export const departmentsApi = {
  list: async () => {
    const res = await api.get<DepartmentResponseDto[]>(`${DEPT_BASE}`);
    return res.data;
  },

  listPaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<DepartmentResponseDto>>(`${DEPT_BASE}/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  getById: async (departmentId: number) => {
    const res = await api.get<DepartmentResponseDto>(`${DEPT_BASE}/${departmentId}`);
    return res.data;
  },

  create: async (body: DepartmentRequestDto) => {
    const res = await api.post<DepartmentResponseDto>(`${DEPT_BASE}`, body);
    return res.data;
  },
};