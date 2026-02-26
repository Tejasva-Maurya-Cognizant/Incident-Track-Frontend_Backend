import { api } from "../../lib/axios/axiosInstance";
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

  getUserById: async (id: number) => {
    const res = await api.get<UserResponseDto>(`${AUTH_BASE}/getUserById/${id}`);
    return res.data;
  },

  getEmployeesByDepartment: async () => {
    const res = await api.get<UserResponseDto[]>(`${AUTH_BASE}/getEmployeesByDepartment`);
    return res.data;
  },

  updateUser: async (body: UpdateUserSelf) => {
    const res = await api.put<string>(`${AUTH_BASE}/updateUser`, body);
    return res.data;
  },

  updateUserDetailsByAdmin: async (id: number, body: UpdateUserDetails) => {
    const res = await api.put<UserResponseDto>(`${AUTH_BASE}/updateUserDetails/${id}`, body);
    return res.data;
  },

  deactivateUser: async (id: number) => {
    const res = await api.patch<void>(`${AUTH_BASE}/deactivateUser/${id}`);
    return res.data;
  },
};