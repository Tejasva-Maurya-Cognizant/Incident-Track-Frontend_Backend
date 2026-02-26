import { api } from "../../lib/axios/axiosInstance";
import type { PagedResponse, PageParams } from "../../types/pagination";
import type { CategoryRequestDto, CategoryResponseDto } from "./types";

const BASE = "/categories";

export const categoriesApi = {
  list: async () => {
    const res = await api.get<CategoryResponseDto[]>(`${BASE}`);
    return res.data;
  },

  listPaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<CategoryResponseDto>>(`${BASE}/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  listVisiblePaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<CategoryResponseDto>>(`${BASE}/visible/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  create: async (body: CategoryRequestDto) => {
    const res = await api.post<CategoryResponseDto>(`${BASE}`, body);
    return res.data;
  },

  parents: async () => {
    const res = await api.get<string[]>(`${BASE}/parent-categories`);
    return res.data;
  },

  subcategories: async (parent: string) => {
    const res = await api.get<string[]>(`${BASE}/subcategories`, { params: { parent } });
    return res.data;
  },

  // Note: backend returns Optional<Category> (entity) — avoid using this in UI unless you add a proper DTO
  // details: async (id: number) => api.get(`${BASE}/details/${id}`),

  update: async (id: number, body: CategoryRequestDto) => {
    const res = await api.patch<CategoryResponseDto>(`${BASE}/details/${id}`, body);
    return res.data;
  },

  toggleVisibility: async (id: number) => {
    const res = await api.patch<CategoryResponseDto>(`${BASE}/visibility/${id}`);
    return res.data;
  },
};