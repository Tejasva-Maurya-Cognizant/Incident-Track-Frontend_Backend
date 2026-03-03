import { api } from "../../lib/axios/axiosInstance";
import type { NotificationResponseDto } from "./types";

export const notificationsApi = {
    /** GET /api/notifications/user — all notifications (read + unread) */
    getAll: async (): Promise<NotificationResponseDto[]> => {
        const res = await api.get<NotificationResponseDto[]>("/notifications/user");
        return res.data;
    },

    /** PATCH /api/notifications/{id}/mark-as-read */
    markAsRead: async (id: number): Promise<void> => {
        await api.patch(`/notifications/${id}/mark-as-read`);
    },

    /** PATCH /api/notifications/user/mark-all-read */
    markAllRead: async (): Promise<void> => {
        await api.patch("/notifications/user/mark-all-read");
    },
};
