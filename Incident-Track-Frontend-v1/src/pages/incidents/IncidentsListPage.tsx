import { useEffect, useMemo, useState, useCallback } from "react";
import { incidentsApi } from "../../features/incidents/api";
import type { IncidentResponseDTO, IncidentSeverity, IncidentStatus } from "../../features/incidents/types";
import type { PageParams } from "../../types/pagination";
import { useAuth } from "../../context/AuthContext";
import StatusBadge from "../../components/common/StatusBadge";
import PriorityBadge from "../../components/common/PriorityBadge";
import Pagination from "../../components/common/Pagination";
import SortableHeader from "../../components/common/SortableHeader";

const DEFAULT_PARAMS: PageParams = { page: 0, size: 10, sortBy: "reportedDate", sortDir: "desc" };

export default function IncidentsListPage() {
  const { user } = useAuth();
  const role = user?.role ?? "EMPLOYEE";

  const canSeeAll = role === "ADMIN" || role === "MANAGER";
  const canUpdateStatus = role === "ADMIN" || role === "MANAGER";

  const [scope, setScope] = useState<"MINE" | "ALL">(canSeeAll ? "ALL" : "MINE");
  const [status, setStatus] = useState<IncidentStatus | "">("");
  const [severity, setSeverity] = useState<IncidentSeverity | "">("");
  const [onlyCritical, setOnlyCritical] = useState(false);

  const [searchId, setSearchId] = useState<string>("");
  const [searchResult, setSearchResult] = useState<IncidentResponseDTO | null>(null);

  const [items, setItems] = useState<IncidentResponseDTO[]>([]);
  const [paging, setPaging] = useState({ totalElements: 0, totalPages: 0, page: 0 });
  const [params, setParams] = useState<PageParams>(DEFAULT_PARAMS);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(async (p: PageParams) => {
    setLoading(true);
    setErr(null);
    setSearchResult(null);
    try {
      let res;
      if (onlyCritical) {
        res = await incidentsApi.listCriticalPaged(p);
      } else if (status) {
        res = await incidentsApi.listByStatusPaged(status, p);
      } else if (severity) {
        res = await incidentsApi.listBySeverityPaged(severity, p);
      } else if (canSeeAll && scope === "ALL") {
        res = await incidentsApi.listAllAdminManagerPaged(p);
      } else {
        res = await incidentsApi.listMinePaged(p);
      }
      setItems(res.content);
      setPaging({ totalElements: res.totalElements, totalPages: res.totalPages, page: res.page });
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Failed to load incidents");
    } finally {
      setLoading(false);
    }
  }, [scope, status, severity, onlyCritical, canSeeAll]);

  // Reload whenever params or filters change
  useEffect(() => {
    load(params);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params, scope, status, severity, onlyCritical]);

  // When a filter changes reset to page 0
  const resetToFirstPage = (newPartial: Partial<PageParams> = {}) => {
    setParams((prev) => ({ ...prev, page: 0, ...newPartial }));
  };

  const handleSort = (field: string) => {
    setParams((prev) => ({
      ...prev,
      page: 0,
      sortBy: field,
      sortDir: prev.sortBy === field && prev.sortDir === "asc" ? "desc" : "asc",
    }));
  };

  const onSearch = async () => {
    const idNum = Number(searchId);
    if (!idNum) return;
    setLoading(true);
    setErr(null);
    try {
      const data = canSeeAll
        ? await incidentsApi.getByIdAdminManager(idNum)
        : await incidentsApi.getByIdMine(idNum);
      setSearchResult(data);
      setItems([data]);
      setPaging({ totalElements: 1, totalPages: 1, page: 0 });
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? `Incident ${searchId} not found / not allowed`);
      setItems([]);
      setPaging({ totalElements: 0, totalPages: 0, page: 0 });
    } finally {
      setLoading(false);
    }
  };

  const clearSearch = () => {
    setSearchId("");
    setSearchResult(null);
    load(params);
  };

  const emptyState = useMemo(() => !loading && items.length === 0, [loading, items]);
  const showPagination = !searchResult && paging.totalPages > 0;

  return (
    <div className="flex flex-col gap-3 h-full">
      {/* Page header */}
      <div className="flex items-center justify-between gap-2 shrink-0" >
        <div>
          <h2 className="text-base font-semibold text-slate-900">Incidents</h2>
          <p className="text-xs text-slate-400 mt-0.5">View, filter, and track incidents.</p>
        </div>
        <a href="/incidents/create" className="btn-primary inline-flex items-center gap-1.5 shrink-0">
          <svg xmlns="http://www.w3.org/2000/svg" className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}><path strokeLinecap="round" strokeLinejoin="round" d="M12 4v16m8-8H4" /></svg>
          Create
        </a>
      </div>

      {/* Controls */}
      <div className="card p-3 shrink-0" style={{ position: "sticky", top: 0, zIndex: 10 }}>
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-2">
          {canSeeAll ? (
            <div>
              <label className="text-xs text-slate-600">Scope</label>
              <select className="input mt-0.5 bg-white" value={scope} onChange={(e) => {
                setScope(e.target.value as any);
                resetToFirstPage();
              }}>
                <option value="ALL">All incidents</option>
                <option value="MINE">My incidents</option>
              </select>
            </div>
          ) : (
            <div className="hidden lg:block" />
          )}

          <div>
            <label className="text-xs text-slate-600">Status</label>
            <select
              className="input mt-0.5 bg-white"
              value={status}
              onChange={(e) => {
                setOnlyCritical(false);
                setSeverity("");
                setStatus(e.target.value as any);
                resetToFirstPage();
              }}
            >
              <option value="">All</option>
              <option value="OPEN">OPEN</option>
              <option value="IN_PROGRESS">IN_PROGRESS</option>
              <option value="RESOLVED">RESOLVED</option>
              <option value="CANCELLED">CANCELLED</option>
            </select>
          </div>

          <div>
            <label className="text-xs text-slate-600">Severity</label>
            <select
              className="input mt-0.5 bg-white"
              value={severity}
              onChange={(e) => {
                setOnlyCritical(false);
                setStatus("");
                setSeverity(e.target.value as any);
                resetToFirstPage();
              }}
            >
              <option value="">All</option>
              <option value="CRITICAL">CRITICAL</option>
              <option value="HIGH">HIGH</option>
              <option value="MEDIUM">MEDIUM</option>
              <option value="LOW">LOW</option>
            </select>
          </div>

          <div className="flex items-end pb-0.5">
            <label className="flex items-center gap-1.5 text-xs text-slate-700">
              <input
                type="checkbox"
                checked={onlyCritical}
                onChange={(e) => {
                  setOnlyCritical(e.target.checked);
                  if (e.target.checked) {
                    setStatus("");
                    setSeverity("");
                  }
                  resetToFirstPage();
                }}
              />
              Only Critical
            </label>
          </div>

          <div>
            <label className="text-xs text-slate-600">Search by ID</label>
            <div className="mt-0.5 flex gap-1.5">
              <input
                className="input"
                value={searchId}
                onChange={(e) => setSearchId(e.target.value)}
                placeholder="e.g. 101"
                onKeyDown={(e) => e.key === "Enter" && onSearch()}
              />
              {searchResult ? (
                <button
                  className="h-9 px-3 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] shrink-0"
                  style={{ borderColor: "var(--border)" }}
                  onClick={clearSearch}
                >
                  Clear
                </button>
              ) : (
                <button
                  className="h-9 px-3 rounded-[8px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] shrink-0"
                  style={{ borderColor: "var(--border)" }}
                  onClick={onSearch}
                >
                  Search
                </button>
              )}
            </div>
          </div>
        </div>

        {err && <div className="text-sm text-red-600 mt-3">{err}</div>}
      </div>

      {/* Table — overflow-x-auto on the card so sticky thead works */}
      <div className="card overflow-auto shrink-0">
        <table className="w-full text-sm min-w-[600px]">
          <thead>
            <tr className="text-xs uppercase tracking-wide text-slate-500" style={{ position: "sticky", top: 0, zIndex: 5, background: "#F8FAFD" }}>
              <SortableHeader label="ID" field="incidentId" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-14" />
              <th className="text-left px-2 py-2 w-28">Category</th>
              <th className="text-left px-2 py-2 w-32">Sub-Category</th>
              <SortableHeader label="Status" field="status" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-24" />
              <SortableHeader label="Severity" field="calculatedSeverity" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-20" />
              <SortableHeader label="Reported" field="reportedDate" sortBy={params.sortBy} sortDir={params.sortDir} onSort={handleSort} className="w-28" />
              <th className="text-right px-2 py-2 w-24">Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr>
                <td className="px-2 py-4 text-slate-600" colSpan={7}>Loading...</td>
              </tr>
            ) : emptyState ? (
              <tr>
                <td className="px-2 py-4 text-slate-600" colSpan={7}>No incidents found.</td>
              </tr>
            ) : (
              items.map((it) => (
                <tr key={it.incidentId} className="border-t hover:bg-[#FAFCFF]" style={{ borderColor: "var(--border)" }}>
                  <td className="px-2 py-2 font-medium text-slate-900 w-14 text-xs">{it.incidentId}</td>
                  <td className="px-2 py-2 text-slate-700 w-28">
                    <span className="block truncate max-w-[112px] text-xs">{it.categoryName}</span>
                  </td>
                  <td className="px-2 py-2 text-slate-600 w-32">
                    <span className="block truncate max-w-[128px] text-xs">{it.subCategory ?? <span className="italic text-slate-400">—</span>}</span>
                  </td>
                  <td className="px-2 py-2 w-24"><StatusBadge status={it.status} /></td>
                  <td className="px-2 py-2 w-20"><PriorityBadge severity={it.calculatedSeverity} /></td>
                  <td className="px-2 py-2 text-slate-700 w-28 whitespace-nowrap text-xs">
                    {new Date(it.reportedDate).toLocaleDateString()}<br />
                    <span className="text-slate-400">{new Date(it.reportedDate).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</span>
                  </td>
                  <td className="px-2 py-2 text-right w-24">
                    <div className="inline-flex gap-1">
                      <a
                        href={`/incidents/${it.incidentId}`}
                        className="h-7 px-2 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] inline-flex items-center"
                        style={{ borderColor: "var(--border)" }}
                      >
                        View
                      </a>
                      {canUpdateStatus && (
                        <a
                          href={`/incidents/${it.incidentId}?mode=status`}
                          className="h-7 px-2 rounded-[6px] bg-white border text-xs font-medium hover:bg-[#FAFCFF] inline-flex items-center"
                          style={{ borderColor: "var(--border)" }}
                        >
                          Update
                        </a>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {showPagination && (
          <Pagination
            page={paging.page}
            totalPages={paging.totalPages}
            totalElements={paging.totalElements}
            size={params.size}
            onPageChange={(newPage) => setParams((prev) => ({ ...prev, page: newPage }))}
            onSizeChange={(newSize) => setParams((prev) => ({ ...prev, size: newSize, page: 0 }))}
          />
        )}
      </div>
    </div>
  );
}