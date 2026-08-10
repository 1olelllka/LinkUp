import { useAuthStore } from "@/store/useAuthStore";
import { useEffect } from "react"
import { toast } from "sonner";

export const useNotificationWebSocket = (userId: string | null) => {
  const token = useAuthStore.getState().token;
  useEffect(() => {
    if (!userId || !token) return; // wait for BOTH — don't connect with a missing token

    let ws;
    let closedByEffect = false;
    let reconnectTimer;
    let attempt = 0;

    const connect = () => {
      ws = new WebSocket(`ws://localhost:8080/notifications?userId=${userId}`);

      ws.onopen = () => {
        attempt = 0; // reset backoff on a healthy connection
        ws.send(JSON.stringify({ token: token }));
      };

      ws.onmessage = (ev) => {
        const text = ev.data.length > 100 ? ev.data.slice(0, 100) + "..." : ev.data;
        toast(text, { position: "top-right" });
      };

      ws.onerror = (err) => console.error("WS error", err);

      ws.onclose = (ev) => {
        console.log("WS closed", ev.code, ev.reason);
        if (closedByEffect) return; // intentional teardown, don't retry
        const delay = Math.min(1000 * 2 ** attempt, 15000);
        attempt += 1;
        reconnectTimer = setTimeout(connect, delay);
      };
    };

    connect();

    return () => {
      closedByEffect = true;
      clearTimeout(reconnectTimer);
      if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) {
        ws.close(1000, "component unmount");
      }
    };
  }, [userId, token]);

  return {};
};
