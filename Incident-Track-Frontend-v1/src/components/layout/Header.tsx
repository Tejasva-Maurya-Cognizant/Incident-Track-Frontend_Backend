import { useMemo, useState, useRef, useEffect } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { useNotifications } from "../../context/NotificationContext";

function getGreeting() {
  const h = new Date().getHours();
  if (h < 12) return "Good morning";
  if (h < 18) return "Good afternoon";
  return "Good evening";
}

/** Tiny inline dropdown showing the 6 most recent notifications */
function NotificationDropdown({ onClose }: { onClose: () => void }) {
  const { notifications, unreadCount, markAsRead, markAllRead } = useNotifications();
  const recent = notifications.slice(0, 6);

  const fmtTime = (iso: string) => {
    const d = new Date(iso);
    const diffMin = Math.floor((Date.now() - d.getTime()) / 60000);
    if (diffMin < 1) return "Just now";
    if (diffMin < 60) return `${diffMin}m ago`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return `${diffH}h ago`;
    return d.toLocaleDateString();
  };

  const typeIcon: Record<string, string> = {
    INCIDENT_REPORTED: "📋",
    CRITICAL_INCIDENT_ALERT: "🚨",
    INCIDENT_WITHDRAWN: "❌",
    TASK_ASSIGNED: "📌",
    INCIDENT_RESOLVED: "✅",
    SLA_BREACHED: "⚠️",
  };

  return (
    <div className="absolute right-0 top-full mt-2 w-80 card shadow-lg z-50 overflow-hidden">
      {/* Dropdown header */}
      <div className="flex items-center justify-between px-3 py-2 border-b" style={{ borderColor: "var(--border)" }}>
        <span className="text-xs font-semibold text-slate-800">
          Notifications{" "}
          {unreadCount > 0 && <span className="text-[#175FFA]">({unreadCount} new)</span>}
        </span>
        <div className="flex items-center gap-3">
          {unreadCount > 0 && (
            <button onClick={markAllRead} className="text-[10px] text-[#175FFA] hover:underline">
              Mark all read
            </button>
          )}
          <Link to="/notifications" onClick={onClose} className="text-[10px] text-slate-500 hover:text-slate-800">
            See all →
          </Link>
        </div>
      </div>

      {/* Items */}
      {recent.length === 0 ? (
        <div className="px-3 py-6 text-center text-xs text-slate-400">No notifications yet.</div>
      ) : (
        <ul className="divide-y max-h-72 overflow-y-auto" style={{ borderColor: "var(--border)" }}>
          {recent.map((n) => (
            <li
              key={n.notificationId}
              className={`px-3 py-2.5 flex gap-2.5 cursor-pointer hover:bg-[#FAFCFF] transition-colors ${n.status === "UNREAD" ? "bg-[#F0F6FF]" : ""
                }`}
              onClick={() => { if (n.status === "UNREAD") markAsRead(n.notificationId); }}
            >
              <span className="text-sm shrink-0 mt-0.5">{typeIcon[n.type] ?? "🔔"}</span>
              <div className="flex-1 min-w-0">
                <p className={`text-xs leading-snug ${n.status === "UNREAD" ? "font-medium text-slate-900" : "text-slate-700"}`}>
                  {n.message}
                </p>
                <p className="text-[10px] text-slate-400 mt-0.5">{fmtTime(n.createdDateTime)}</p>
              </div>
              {n.status === "UNREAD" && (
                <span className="w-1.5 h-1.5 rounded-full bg-[#175FFA] shrink-0 mt-1.5" />
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default function Header({ onMenuClick }: { onMenuClick?: () => void }) {
  const { user } = useAuth();
  const { unreadCount } = useNotifications();
  const [dropOpen, setDropOpen] = useState(false);
  const dropRef = useRef<HTMLDivElement>(null);

  const greeting = useMemo(() => getGreeting(), []);
  const role = user?.role ?? "—";

  // Close dropdown on outside click
  useEffect(() => {
    if (!dropOpen) return;
    const handler = (e: MouseEvent) => {
      if (dropRef.current && !dropRef.current.contains(e.target as Node)) {
        setDropOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, [dropOpen]);

  return (
    <header className="card px-3 py-2.5 sm:px-4 sm:py-3">
      <div className="flex items-center justify-between gap-3">
        {/* Left: hamburger + greeting */}
        <div className="flex items-center gap-2.5">
          <button
            className="lg:hidden h-8 w-8 rounded-lg border flex items-center justify-center text-slate-600 hover:bg-[#FAFCFF] shrink-0"
            style={{ borderColor: "var(--border)" }}
            onClick={onMenuClick}
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <div>
            <div className="text-xs text-slate-500">{greeting},</div>
            <div className="text-base font-semibold text-slate-900 leading-tight">
              Hi, {user?.username ?? "there"} 👋{" "}
              <span className="text-xs font-medium text-slate-500">({role})</span>
            </div>
          </div>
        </div>

        {/* Right: bell */}
        <div className="relative" ref={dropRef}>
          <button
            onClick={() => setDropOpen((v) => !v)}
            className="relative h-8 w-8 rounded-lg border flex items-center justify-center text-slate-600 hover:bg-[#FAFCFF] transition-colors"
            style={{ borderColor: "var(--border)" }}
            aria-label="Notifications"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
            </svg>
            {unreadCount > 0 && (
              <span className="absolute -top-1 -right-1 min-w-[16px] h-4 px-0.5 rounded-full bg-[#175FFA] text-white text-[9px] font-bold flex items-center justify-center leading-none">
                {unreadCount > 99 ? "99+" : unreadCount}
              </span>
            )}
          </button>

          {dropOpen && <NotificationDropdown onClose={() => setDropOpen(false)} />}
        </div>
      </div>
    </header>
  );
}