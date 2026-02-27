import { useState } from "react";
import { useNotifications } from "../../context/NotificationContext";
import type { NotificationType } from "../../features/notifications/types";

const TYPE_LABELS: Record<NotificationType, string> = {
  INCIDENT_REPORTED: "Incident Reported",
  CRITICAL_INCIDENT_ALERT: "Critical Alert",
  INCIDENT_WITHDRAWN: "Incident Withdrawn",
  TASK_ASSIGNED: "Task Assigned",
  INCIDENT_RESOLVED: "Incident Resolved",
  SLA_BREACHED: "SLA Breached",
};

const TYPE_ICON: Record<NotificationType, string> = {
  INCIDENT_REPORTED: "📋",
  CRITICAL_INCIDENT_ALERT: "🚨",
  INCIDENT_WITHDRAWN: "❌",
  TASK_ASSIGNED: "📌",
  INCIDENT_RESOLVED: "✅",
  SLA_BREACHED: "⚠️",
};

const TYPE_COLOR: Record<NotificationType, string> = {
  INCIDENT_REPORTED: "bg-blue-50 text-blue-700 border-blue-200",
  CRITICAL_INCIDENT_ALERT: "bg-red-50 text-red-700 border-red-200",
  INCIDENT_WITHDRAWN: "bg-slate-50 text-slate-600 border-slate-200",
  TASK_ASSIGNED: "bg-purple-50 text-purple-700 border-purple-200",
  INCIDENT_RESOLVED: "bg-green-50 text-green-700 border-green-200",
  SLA_BREACHED: "bg-orange-50 text-orange-700 border-orange-200",
};

type FilterTab = "ALL" | "UNREAD" | "READ";

export default function NotificationsPage() {
  const { notifications, unreadCount, loading, markAsRead, markAllRead } = useNotifications();
  const [tab, setTab] = useState<FilterTab>("ALL");

  const displayed =
    tab === "UNREAD"
      ? notifications.filter((n) => n.status === "UNREAD")
      : tab === "READ"
      ? notifications.filter((n) => n.status === "READ")
      : notifications;

  const fmtDate = (iso: string) => {
    const d = new Date(iso);
    const diffMin = Math.floor((Date.now() - d.getTime()) / 60000);
    if (diffMin < 1) return "Just now";
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return `${diffH}h ago`;
    return (
      d.toLocaleDateString() +
      " · " +
      d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
    );
  };

  const tabClass = (t: FilterTab) =>
    `px-3 py-1.5 text-xs rounded-[8px] font-medium transition-colors ${
      tab === t
        ? "bg-white border shadow-sm text-slate-900"
        : "text-slate-500 hover:text-slate-700 hover:bg-white/60"
    }`;

  return (
    <div className="flex flex-col gap-3 h-full">
      {/* Page header */}
      <div className="flex items-center justify-between shrink-0">
        <div>
          <h2 className="text-base font-semibold text-slate-900">Notifications</h2>
          <p className="text-xs text-slate-400 mt-0.5">
            {unreadCount > 0 ? `${unreadCount} unread notification${unreadCount !== 1 ? "s" : ""}` : "All caught up!"}
          </p>
        </div>
        {unreadCount > 0 && (
          <button
            onClick={markAllRead}
            className="h-8 px-3.5 rounded-[8px] border text-xs font-medium text-slate-700 hover:bg-[#FAFCFF] transition-colors"
            style={{ borderColor: "var(--border)" }}
          >
            Mark all as read
          </button>
        )}
      </div>

      {/* Filter tabs */}
      <div
        className="flex items-center gap-1 p-1 rounded-[10px] shrink-0 w-fit"
        style={{ background: "var(--bg)", border: "1px solid var(--border)" }}
      >
        {(["ALL", "UNREAD", "READ"] as FilterTab[]).map((t) => (
          <button key={t} className={tabClass(t)} style={tab === t ? { borderColor: "var(--border)" } : {}} onClick={() => setTab(t)}>
            {t === "ALL" ? `All (${notifications.length})` : t === "UNREAD" ? `Unread (${notifications.filter((n) => n.status === "UNREAD").length})` : `Read (${notifications.filter((n) => n.status === "READ").length})`}
          </button>
        ))}
      </div>

      {/* Notification list */}
      <div className="card flex flex-col flex-1 overflow-hidden">
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-500">Loading notifications…</div>
        ) : displayed.length === 0 ? (
          <div className="py-12 text-center">
            <div className="text-3xl mb-2">🔔</div>
            <div className="text-sm font-medium text-slate-700">
              {tab === "UNREAD" ? "No unread notifications" : tab === "READ" ? "No read notifications" : "No notifications yet"}
            </div>
            <p className="text-xs text-slate-400 mt-1">You're all caught up!</p>
          </div>
        ) : (
          <ul className="divide-y overflow-y-auto flex-1" style={{ borderColor: "var(--border)" }}>
            {displayed.map((n) => (
              <li
                key={n.notificationId}
                className={`flex gap-3 px-4 py-3 transition-colors ${
                  n.status === "UNREAD"
                    ? "bg-[#F0F6FF] hover:bg-[#E8F0FE] cursor-pointer"
                    : "hover:bg-[#FAFCFF]"
                }`}
                onClick={() => { if (n.status === "UNREAD") markAsRead(n.notificationId); }}
              >
                {/* Icon */}
                <div className="shrink-0 mt-0.5 text-lg leading-none">
                  {TYPE_ICON[n.type] ?? "🔔"}
                </div>

                {/* Body */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-start justify-between gap-2">
                    <div className="flex items-center gap-1.5 flex-wrap">
                      <span
                        className={`inline-flex items-center text-[10px] font-semibold px-1.5 py-0.5 rounded-full border ${TYPE_COLOR[n.type]}`}
                      >
                        {TYPE_LABELS[n.type]}
                      </span>
                      {n.status === "UNREAD" && (
                        <span className="w-1.5 h-1.5 rounded-full bg-[#175FFA] shrink-0" />
                      )}
                    </div>
                    <span className="text-[10px] text-slate-400 shrink-0 whitespace-nowrap">
                      {fmtDate(n.createdDateTime)}
                    </span>
                  </div>
                  <p className={`text-xs mt-1 leading-relaxed ${n.status === "UNREAD" ? "text-slate-900 font-medium" : "text-slate-600"}`}>
                    {n.message}
                  </p>
                </div>

                {/* Mark read button on unread */}
                {n.status === "UNREAD" && (
                  <button
                    className="shrink-0 self-start mt-0.5 text-[10px] text-[#175FFA] hover:underline whitespace-nowrap"
                    onClick={(e) => { e.stopPropagation(); markAsRead(n.notificationId); }}
                  >
                    Mark read
                  </button>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
