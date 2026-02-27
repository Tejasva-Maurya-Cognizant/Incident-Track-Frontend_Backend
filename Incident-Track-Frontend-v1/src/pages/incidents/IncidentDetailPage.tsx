import { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import { incidentsApi } from "../../features/incidents/api";
import type { IncidentResponseDTO, IncidentStatus } from "../../features/incidents/types";
import { useAuth } from "../../context/AuthContext";
import StatusBadge from "../../components/common/StatusBadge";
import PriorityBadge from "../../components/common/PriorityBadge";

const ALL_STATUSES: IncidentStatus[] = ["OPEN", "IN_PROGRESS", "RESOLVED", "CANCELLED"];

function InfoRow({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div
      className="flex flex-col gap-1 py-2 border-b last:border-0 sm:flex-row sm:items-start sm:justify-between"
      style={{ borderColor: "var(--border)" }}
    >
      <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wide w-full shrink-0 sm:w-28">
        {label}
      </span>
      <span className="text-xs text-slate-800 font-medium text-left sm:text-right">{children}</span>
    </div>
  );
}

export default function IncidentDetailPage() {
  const { id } = useParams();
  const incidentId = Number(id);
  const navigate = useNavigate();

  const { user } = useAuth();
  const role = user?.role ?? "EMPLOYEE";
  const canSeeAll = role === "ADMIN" || role === "MANAGER";
  const canUpdateStatus = role === "ADMIN" || role === "MANAGER";
  const canWithdraw = role === "EMPLOYEE";

  const [data, setData] = useState<IncidentResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [newStatus, setNewStatus] = useState<IncidentStatus>("IN_PROGRESS");
  const [note, setNote] = useState("");
  const [updating, setUpdating] = useState(false);
  const [updateErr, setUpdateErr] = useState<string | null>(null);
  const [updateOk, setUpdateOk] = useState(false);
  const [withdrawing, setWithdrawing] = useState(false);
  const [withdrawErr, setWithdrawErr] = useState<string | null>(null);

  const load = async () => {
    setLoading(true);
    setErr(null);
    try {
      const res = canSeeAll
        ? await incidentsApi.getByIdAdminManager(incidentId)
        : await incidentsApi.getByIdMine(incidentId);
      setData(res);
      setNewStatus(res.status);
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? "Failed to load incident");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!incidentId) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [incidentId]);

  const onWithdraw = async () => {
    setWithdrawing(true);
    setWithdrawErr(null);
    try {
      const res = await incidentsApi.withdraw(incidentId);
      setData(res);
    } catch (e: any) {
      setWithdrawErr(e?.response?.data?.message ?? "Withdraw failed");
    } finally {
      setWithdrawing(false);
    }
  };

  const onUpdateStatus = async () => {
    if (!canUpdateStatus) return;
    setUpdating(true);
    setUpdateErr(null);
    setUpdateOk(false);
    try {
      const res = await incidentsApi.updateStatusAdminManager(incidentId, { status: newStatus, note });
      setData(res);
      setNote("");
      setUpdateOk(true);
    } catch (e: any) {
      setUpdateErr(e?.response?.data?.message ?? "Status update failed");
    } finally {
      setUpdating(false);
    }
  };

  const fmtDate = (iso: string | null | undefined) => {
    if (!iso) return "—";
    const d = new Date(iso);
    return d.toLocaleDateString() + " " + d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  };

  if (loading)
    return <div className="text-xs text-slate-500 py-8 text-center">Loading incident</div>;

  if (err)
    return (
      <div className="card p-6 text-center">
        <p className="text-xs text-red-600 mb-3">{err}</p>
        <Link to="/incidents" className="text-xs text-[#175FFA] hover:underline">Back to Incidents</Link>
      </div>
    );

  if (!data) return null;

  return (
    <div className="page-panel space-y-3">
      {/* Breadcrumb */}
      <div className="flex items-center gap-1.5 text-xs text-slate-500">
        <Link to="/incidents" className="hover:text-[#175FFA] transition-colors">Incidents</Link>
        <span>/</span>
        <span className="text-slate-900 font-medium">Incident #{data.incidentId}</span>
      </div>

      {/* Page heading + actions */}
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-base font-semibold text-slate-900 leading-tight">{data.categoryName}</h2>
          <p className="text-[11px] text-slate-400 mt-0.5">
            Incident #{data.incidentId}{data.subCategory ? `  ${data.subCategory}` : ""}
          </p>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          {canWithdraw && data.status !== "CANCELLED" && data.status !== "RESOLVED" && (
            <button
              onClick={onWithdraw}
              disabled={withdrawing}
              className="h-8 px-3.5 rounded-[8px] border text-xs font-medium text-slate-700 hover:bg-[#FAFCFF] transition-colors"
              style={{ borderColor: "var(--border)" }}
            >
              {withdrawing ? "Withdrawing" : "Withdraw"}
            </button>
          )}
          <button
            onClick={() => navigate(-1)}
            className="inline-flex items-center gap-1 h-8 px-3.5 rounded-[8px] border text-xs font-medium text-slate-700 hover:bg-[#FAFCFF] transition-colors"
            style={{ borderColor: "var(--border)" }}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}><path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" /></svg>
            Back
          </button>
        </div>
      </div>

      {/* Detail card */}
      <div className="card p-4">
        {/* Description */}
        <div className="mb-3 pb-3 border-b" style={{ borderColor: "var(--border)" }}>
          <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wide mb-1">Description</div>
          <p className="text-xs text-slate-700 whitespace-pre-wrap leading-relaxed">{data.description}</p>
        </div>

        {/* 2-column info grid */}
        <div className="grid grid-cols-1 gap-x-6 gap-y-0 md:grid-cols-2">
          <InfoRow label="Status"><StatusBadge status={data.status} /></InfoRow>
          <InfoRow label="Severity"><PriorityBadge severity={data.calculatedSeverity} /></InfoRow>
          <InfoRow label="Category">{data.categoryName}</InfoRow>
          <InfoRow label="Sub-Category">{data.subCategory ?? "—"}</InfoRow>
          <InfoRow label="Department">{data.departmentName ?? "—"}</InfoRow>
          <InfoRow label="SLA Hours">{data.slaHours != null ? `${data.slaHours}h` : "—"}</InfoRow>
          <InfoRow label="Reported By">{data.username ?? "—"}</InfoRow>
          <InfoRow label="Critical">{data.isCritical ? "Yes" : "No"}</InfoRow>
          <InfoRow label="Reported At">{fmtDate(data.reportedDate)}</InfoRow>
          {data.resolvedDate && <InfoRow label="Resolved At">{fmtDate(data.resolvedDate)}</InfoRow>}
        </div>

        {/* Withdraw error */}
        {withdrawErr && (
          <div className="flex items-center gap-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-[8px] px-3 py-1.5 mt-3">
            {withdrawErr}
          </div>
        )}

        {/* Inline status update for admin/manager */}
        {canUpdateStatus && (
          <div className="mt-3 pt-3 border-t" style={{ borderColor: "var(--border)" }}>
            <div className="text-[10px] font-semibold text-slate-400 uppercase tracking-wide mb-2">Update Status</div>
            <div className="flex flex-wrap items-center gap-1.5">
              {ALL_STATUSES.map((s) => (
                <button
                  key={s}
                  onClick={() => { setNewStatus(s); setUpdateOk(false); setUpdateErr(null); }}
                  className={`h-7 px-3 rounded-[8px] text-xs font-medium border transition-colors ${newStatus === s
                    ? "bg-[#175FFA] text-white border-[#175FFA]"
                    : "border-[var(--border)] text-slate-600 hover:bg-[#FAFCFF]"
                    }`}
                >
                  {s === "IN_PROGRESS" ? "In Progress" : s.charAt(0) + s.slice(1).toLowerCase()}
                </button>
              ))}
              <button
                className="btn-primary h-7 text-xs px-3 ml-auto"
                onClick={onUpdateStatus}
                disabled={updating || data.status === newStatus}
              >
                {updating ? "Updating" : "Apply"}
              </button>
            </div>
            <input
              className="input mt-2 h-8 text-xs"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Optional note (reason for status change)"
            />
            {updateErr && (
              <div className="flex items-center gap-2 text-xs text-red-600 bg-red-50 border border-red-200 rounded-[8px] px-3 py-1.5 mt-2">
                {updateErr}
              </div>
            )}
            {updateOk && (
              <div className="flex items-center gap-2 text-xs text-green-700 bg-green-50 border border-green-200 rounded-[8px] px-3 py-1.5 mt-2">
                Status updated to {newStatus}.
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
