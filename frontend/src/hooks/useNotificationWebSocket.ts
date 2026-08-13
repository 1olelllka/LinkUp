import { useAuthStore } from "@/store/useAuthStore";
import { useEffect } from "react";
import { toast } from "sonner";

export const useNotificationWebSocket = (userId: string | null) => {
  const token = useAuthStore.getState().token;

  useEffect(() => {
    if (!userId || !token) return;

    let ws: WebSocket | null = null;
    let closedByEffect = false;
    let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
    let attempt = 0;

    const connect = () => {
      ws = new WebSocket(
        `ws://localhost:8080/notifications?userId=${userId}`
      );

      ws.onopen = () => {
        attempt = 0;

        ws?.send(
          JSON.stringify({
            token,
          })
        );
      };

      ws.onmessage = (ev: MessageEvent) => {
        const data = String(ev.data);
        const text =
          data.length > 100
            ? `${data.slice(0, 100)}...`
            : data;

        toast(text, {
          position: "top-right",
        });
      };

      ws.onerror = (err: Event) => {
        console.error("WS error", err);
      };

      ws.onclose = (ev: CloseEvent) => {
        console.log("WS closed", ev.code, ev.reason);

        if (closedByEffect) return;

        const delay = Math.min(
          1000 * 2 ** attempt,
          15000
        );

        attempt += 1;

        reconnectTimer = setTimeout(connect, delay);
      };
    };

    connect();

    return () => {
      closedByEffect = true;

      if (reconnectTimer) {
        clearTimeout(reconnectTimer);
        reconnectTimer = null;
      }

      if (
        ws &&
        (ws.readyState === WebSocket.OPEN ||
          ws.readyState === WebSocket.CONNECTING)
      ) {
        ws.close(1000, "component unmount");
      }

      ws = null;
    };
  }, [userId, token]);

  return {};
};