import { api } from "../../lib/axios/axiosInstance";
import type { PagedResponse, PageParams } from "../../types/pagination";
import type { AuditLogResponseDto, SlaBreachResponseDto } from "./types";

const BASE = "/admin/compliance";

export const complianceApi = {
    // ── Audit Logs ────────────────────────────────────────────────────────────

    /** GET /api/admin/compliance/audit-logs/paged  (ADMIN only) */
    getAuditLogsPaged: async (p: PageParams) => {
        const res = await api.get<PagedResponse<AuditLogResponseDto>>(`${BASE}/audit-logs/paged`, {
            params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
        });
        return res.data;
    },

    /** GET /api/admin/compliance/audit-logs/{incidentId}/paged  (ADMIN only) */
    getAuditLogsByIncidentPaged: async (incidentId: number, p: PageParams) => {
        const res = await api.get<PagedResponse<AuditLogResponseDto>>(
            `${BASE}/audit-logs/${incidentId}/paged`,
            { params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir } },
        );
        return res.data;
    },

    /** GET /api/admin/compliance/audit-log/{actionType}/paged  (ADMIN only) */
    getAuditLogsByActionTypePaged: async (actionType: string, p: PageParams) => {
        const res = await api.get<PagedResponse<AuditLogResponseDto>>(
            `${BASE}/audit-log/${actionType}/paged`,
            { params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir } },
        );
        return res.data;
    },

    // ── SLA Breaches ──────────────────────────────────────────────────────────

    /** GET /api/admin/compliance/breaches/paged  (ADMIN + MANAGER) */
    getBreachesPaged: async (p: PageParams) => {
        const res = await api.get<PagedResponse<SlaBreachResponseDto>>(`${BASE}/breaches/paged`, {
            params: { page: p.page, size: p.size, sortBy: p.sortBy, sortDir: p.sortDir },
        });
        return res.data;
    },
};
