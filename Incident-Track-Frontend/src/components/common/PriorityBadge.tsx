import type { IncidentSeverity } from "../../features/incidents/types";

const styles: Record<IncidentSeverity, string> = {
  CRITICAL: "bg-[#FEE0E3] text-[#B42318]",
  HIGH: "bg-[#FEE0E3] text-[#B42318]",
  MEDIUM: "bg-[#E6EDF5] text-slate-700",
  LOW: "bg-[#E6EDF5] text-slate-700",
};

export default function PriorityBadge({ severity }: { severity: IncidentSeverity }) {
  return (
    <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${styles[severity]}`}>
      {severity}
    </span>
  );
}