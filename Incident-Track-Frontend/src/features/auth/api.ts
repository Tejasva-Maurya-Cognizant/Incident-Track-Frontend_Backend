import { api } from "../../lib/axios/axiosInstance";
import type { PagedResponse, PageParams } from "../../types/pagination";
import type {
  AuthRequest,
  AuthResponse,
  UserResponseDto,
  UserRegistrationDTO,
  UpdateUserDetails,
  UpdateUserSelf,
} from "./types";

const AUTH_BASE = "/auth";

export const authApi = {
  login: async (body: AuthRequest) => {
    const res = await api.post<AuthResponse>(`${AUTH_BASE}/login`, body);
    return res.data;
  },

  register: async (body: UserRegistrationDTO) => {
    const res = await api.post<UserResponseDto>(`${AUTH_BASE}/register`, body);
    return res.data;
  },

  viewProfile: async () => {
    const res = await api.get<UserResponseDto>(`${AUTH_BASE}/view-profile`);
    return res.data;
  },

  getAllUsers: async () => {
    const res = await api.get<UserResponseDto[]>(`${AUTH_BASE}/getAllUsers`);
    return res.data;
  },

  getAllUsersPaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<UserResponseDto>>(`${AUTH_BASE}/getAllUsers/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  getUserById: async (id: number) => {
    const res = await api.get<UserResponseDto>(`${AUTH_BASE}/getUserById/${id}`);
    return res.data;
  },

  getEmployeesByDepartment: async () => {
    const res = await api.get<UserResponseDto[]>(`${AUTH_BASE}/getEmployeesByDepartment`);
    return res.data;
  },

  getEmployeesByDepartmentPaged: async (p: PageParams) => {
    const res = await api.get<PagedResponse<UserResponseDto>>(`${AUTH_BASE}/getEmployeesByDepartment/paged`, {
      params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
    });
    return res.data;
  },

  updateUser: async (body: UpdateUserSelf) => {
    const res = await api.put<string>(`${AUTH_BASE}/updateUser`, body);
    return res.data;
  },

  updateUserDetailsByAdmin: async (id: number, body: Partial<UpdateUserDetails>) => {
    const res = await api.put<string>(`${AUTH_BASE}/updateUserDetails/${id}`, body);
    return res.data;
  },

  toggleUserStatus: async (id: number) => {
    const res = await api.patch<void>(`${AUTH_BASE}/toggleUserStatus/${id}`);
    return res.data;
  },
};
