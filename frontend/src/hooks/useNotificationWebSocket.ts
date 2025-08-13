import { useEffect } from "react"
import { toast } from "sonner";

export const useNotificationWebSocket = (userId: string | null) => {
  useEffect(() => {
    if (!userId) return;

    const ws = new WebSocket(`ws://localhost:8080/notifications?userId=${userId}`);
    console.log("Connecting WS...");

    ws.onopen = () => console.log("WS connected");
    ws.onmessage = (ev) => {
        console.log("received message");
        toast(ev.data);
    }
    ws.onerror = (err) => console.error("WS error", err);
    ws.onclose = () => console.log("WS closed");

    return () => {
      ws.close();
      console.log("WS cleanup");
    };
  }, [userId]);

  return {};
};
