import { useEffect, useState } from "react";
import { useParams, useSearchParams } from "react-router-dom";
import { incidentsApi } from "../../features/incidents/api";
import type { IncidentResponseDTO, IncidentStatus } from "../../features/incidents/types";
import { useAuth } from "../../context/AuthContext";
import StatusBadge from "../../components/common/StatusBadge";
import PriorityBadge from "../../components/common/PriorityBadge";

export default function IncidentDetailPage() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const mode = searchParams.get("mode"); // "status" optional

  const { user } = useAuth();
  const role = user?.role ?? "EMPLOYEE";
  const canSeeAll = role === "ADMIN" || role === "MANAGER";
  const canUpdateStatus = role === "ADMIN" || role === "MANAGER";

  const incidentId = Number(id);

  const [data, setData] = useState<IncidentResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  const [newStatus, setNewStatus] = useState<IncidentStatus>("IN_PROGRESS");
  const [note, setNote] = useState("");

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
    try {
      const res = await incidentsApi.withdraw(incidentId);
      setData(res);
    } catch (e: any) {
      alert(e?.response?.data?.message ?? "Withdraw failed");
    }
  };

  const onUpdateStatus = async () => {
    if (!canUpdateStatus) return;
    try {
      const res = await incidentsApi.updateStatusAdminManager(incidentId, { status: newStatus, note });
      setData(res);
      setNote("");
      alert("Status updated");
    } catch (e: any) {
      alert(e?.response?.data?.message ?? "Status update failed");
    }
  };

  if (loading) return <div className="text-sm text-slate-600">Loading...</div>;
  if (err) return <div className="text-sm text-red-600">{err}</div>;
  if (!data) return null;

  return (
    <div className="space-y-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h2 className="text-lg font-semibold text-slate-900">Incident #{data.incidentId}</h2>
          <p className="text-sm text-slate-600 mt-1">{data.categoryName}</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={onWithdraw}
            className="h-[46px] px-4 rounded-[10px] bg-white border text-sm font-medium hover:bg-[#FAFCFF]"
            style={{ borderColor: "var(--border)" }}
          >
            Withdraw
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <div className="card p-5 lg:col-span-2">
          <div className="text-sm font-medium text-slate-900">Description</div>
          <p className="text-sm text-slate-700 mt-2 whitespace-pre-wrap">{data.description}</p>
        </div>

        <div className="card p-5 space-y-3">
          <div className="flex items-center justify-between">
            <div className="text-sm text-slate-600">Status</div>
            <StatusBadge status={data.status} />
          </div>
          <div className="flex items-center justify-between">
            <div className="text-sm text-slate-600">Severity</div>
            <PriorityBadge severity={data.calculatedSeverity} />
          </div>
          <div className="flex items-center justify-between">
            <div className="text-sm text-slate-600">Critical</div>
            <div className="text-sm font-medium text-slate-900">{data.isCritical ? "Yes" : "No"}</div>
          </div>
          <div className="flex items-center justify-between">
            <div className="text-sm text-slate-600">Reported By</div>
            <div className="text-sm font-medium text-slate-900">{data.username}</div>
          </div>
          <div className="flex items-center justify-between">
            <div className="text-sm text-slate-600">Reported At</div>
            <div className="text-sm font-medium text-slate-900">{new Date(data.reportedDate).toLocaleString()}</div>
          </div>
          {data.slaHours != null && (
            <div className="flex items-center justify-between">
              <div className="text-sm text-slate-600">SLA Hours</div>
              <div className="text-sm font-medium text-slate-900">{data.slaHours}</div>
            </div>
          )}
        </div>
      </div>

      {canUpdateStatus && mode === "status" && (
        <div className="card p-5 space-y-3">
          <div className="text-sm font-medium text-slate-900">Update Status</div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="text-sm text-slate-700">New Status</label>
              <select className="input mt-1 bg-white" value={newStatus} onChange={(e) => setNewStatus(e.target.value as any)}>
                <option value="OPEN">OPEN</option>
                <option value="IN_PROGRESS">IN_PROGRESS</option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="CANCELLED">CANCELLED</option>
              </select>
            </div>
            <div>
              <label className="text-sm text-slate-700">Note (optional)</label>
              <input className="input mt-1" value={note} onChange={(e) => setNote(e.target.value)} placeholder="Why status changed?" />
            </div>
          </div>

          <button className="btn-primary" onClick={onUpdateStatus}>
            Update Status
          </button>
        </div>
      )}
    </div>
  );
}