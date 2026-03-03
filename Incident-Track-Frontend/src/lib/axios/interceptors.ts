import type { AxiosError } from "axios";
import { api } from "./axiosInstance";
import { TOKEN_STORAGE_KEY } from "../../config/constants";

export const attachInterceptors = (onUnauthorized?: () => void) => {
  api.interceptors.request.use((config) => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });

  api.interceptors.response.use(
    (res) => res,
    (err: AxiosError) => {
      const status = err.response?.status;
      if (status === 401) onUnauthorized?.();
      return Promise.reject(err);
    }
  );
};