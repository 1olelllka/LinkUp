import { useAuthStore } from "@/store/useAuthStore";
import { useEffect } from "react"
import { toast } from "sonner";

export const useNotificationWebSocket = (userId: string | null) => {
  const token = useAuthStore.getState().token;
  useEffect(() => {
    if (!userId) return;

    const ws = new WebSocket(`ws://localhost:8080/notifications?userId=${userId}`);
    console.log("Connecting WS...");

    ws.onopen = () => {
      ws.send(JSON.stringify({token: token}))
    }
    ws.onmessage = (ev) => {
        const text =
        ev.data.length > 100
          ? ev.data.slice(0, 100) + "..."
          : ev.data;
        toast(text, {position: "top-right"});
    }
    ws.onerror = (err) => console.error("WS error", err);
    ws.onclose = () => console.log("WS closed");

    return () => {
      ws.close();
      console.log("WS cleanup");
    };
  }, [userId, token]);

  return {};
};
