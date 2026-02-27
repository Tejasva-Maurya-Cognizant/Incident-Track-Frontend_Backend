import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import type { ReactNode } from "react";
import { notificationsApi } from "../features/notifications/api";
import type { NotificationResponseDto } from "../features/notifications/types";
import { TOKEN_STORAGE_KEY, API_BASE_URL } from "../config/constants";
import { useAuth } from "./AuthContext";

interface NotificationContextValue {
  notifications: NotificationResponseDto[];
  unreadCount: number;
  loading: boolean;
  markAsRead: (id: number) => Promise<void>;
  markAllRead: () => Promise<void>;
  refresh: () => Promise<void>;
}

const NotificationContext = createContext<NotificationContextValue>({
  notifications: [],
  unreadCount: 0,
  loading: false,
  markAsRead: async () => { },
  markAllRead: async () => { },
  refresh: async () => { },
});

export function NotificationProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState<NotificationResponseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const sseRef = useRef<EventSource | null>(null);
  const retryRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const retryCount = useRef(0);
  // Tracks whether a connect() call is already scheduled/running to prevent storms
  const connectingRef = useRef(false);

  const unreadCount = notifications.filter((n) => n.status === "UNREAD").length;

  const refresh = useCallback(async () => {
    if (!user) return;
    setLoading(true);
    try {
      const data = await notificationsApi.getAll();
      setNotifications(data);
    } catch {
      // silently fail
    } finally {
      setLoading(false);
    }
  }, [user]);

  // Initial load
  useEffect(() => {
    refresh();
  }, [refresh]);

  // SSE connection with auto-reconnect
  useEffect(() => {
    if (!user) return;

    let destroyed = false;

    const connect = () => {
      const token = localStorage.getItem(TOKEN_STORAGE_KEY);
      if (!token || destroyed) return;

      // If a connection is already open or connecting, don't create another one
      if (
        sseRef.current &&
        (sseRef.current.readyState === EventSource.OPEN ||
          sseRef.current.readyState === EventSource.CONNECTING)
      ) {
        connectingRef.current = false;
        return;
      }

      // Close any stale closed connection
      sseRef.current?.close();
      connectingRef.current = true;

      const url = `${API_BASE_URL}/notifications/subscribe?token=${encodeURIComponent(token)}`;
      const es = new EventSource(url);
      sseRef.current = es;

      es.addEventListener("notification", (e: MessageEvent) => {
        retryCount.current = 0; // reset backoff on successful event
        try {
          const incoming: NotificationResponseDto = JSON.parse(e.data);
          setNotifications((prev) => {
            // Avoid duplicates if refresh already added it
            if (prev.some((n) => n.notificationId === incoming.notificationId)) return prev;
            return [incoming, ...prev];
          });
        } catch {
          // ignore malformed events
        }
      });

      es.onopen = () => {
        retryCount.current = 0;
        connectingRef.current = false;
      };

      es.onerror = () => {
        es.close();
        sseRef.current = null;
        connectingRef.current = false;
        if (destroyed) return;
        // Cap retries: stop after 10 attempts (max ~30s backoff each = ~5 min total)
        if (retryCount.current >= 10) {
          return;
        }
        // Exponential backoff: 3s, 6s, 12s … capped at 30s
        const delay = Math.min(3000 * Math.pow(2, retryCount.current), 30000);
        retryCount.current += 1;
        retryRef.current = setTimeout(() => {
          if (!destroyed && !connectingRef.current) connect();
        }, delay);
      };
    };

    connect();

    return () => {
      destroyed = true;
      if (retryRef.current) clearTimeout(retryRef.current);
      sseRef.current?.close();
      sseRef.current = null;
    };
  }, [user]);

  const markAsRead = useCallback(async (id: number) => {
    await notificationsApi.markAsRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.notificationId === id ? { ...n, status: "READ" } : n))
    );
  }, []);

  const markAllRead = useCallback(async () => {
    await notificationsApi.markAllRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, status: "READ" })));
  }, []);

  return (
    <NotificationContext.Provider
      value={{ notifications, unreadCount, loading, markAsRead, markAllRead, refresh }}
    >
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  return useContext(NotificationContext);
}
