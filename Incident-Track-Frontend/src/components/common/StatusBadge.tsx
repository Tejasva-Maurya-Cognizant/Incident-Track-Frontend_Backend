import type { IncidentStatus } from "../../features/incidents/types";

const styles: Record<IncidentStatus, string> = {
  OPEN: "bg-[#FDF7C0] text-[#8A5A00]",
  IN_PROGRESS: "bg-[#DCEBFF] text-[#1D4ED8]",
  RESOLVED: "bg-[#DCFDFC] text-[#0F766E]",
  CANCELLED: "bg-[#E6EDF5] text-slate-700",
};

export default function StatusBadge({ status }: { status: IncidentStatus }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${styles[status]}`}>
      {status}
    </span>
  );
}