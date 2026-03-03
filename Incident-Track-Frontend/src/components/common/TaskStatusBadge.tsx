import type { TaskStatus } from "../../features/tasks/types";

const styles: Record<TaskStatus, string> = {
    PENDING: "bg-[#FDF7C0] text-[#8A5A00]",
    IN_PROGRESS: "bg-[#DCEBFF] text-[#1D4ED8]",
    COMPLETED: "bg-[#DCFDFC] text-[#0F766E]",
};

const labels: Record<TaskStatus, string> = {
    PENDING: "Pending",
    IN_PROGRESS: "In Progress",
    COMPLETED: "Completed",
};

export default function TaskStatusBadge({ status }: { status: TaskStatus }) {
    return (
        <span
            className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-semibold ${styles[status] ?? "bg-slate-100 text-slate-600"}`}
        >
            {labels[status] ?? status}
        </span>
    );
}
