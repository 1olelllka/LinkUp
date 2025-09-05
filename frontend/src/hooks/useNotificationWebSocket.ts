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
        console.log("received message");
        toast(ev.data, {position: "top-right"});
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
