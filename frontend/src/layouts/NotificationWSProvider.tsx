import { useNotificationWebSocket } from "@/hooks/useNotificationWebSocket";
import { useProfileStore } from "@/store/useProfileStore";

export const NotificationWSProvider = ({children} : {children: React.ReactNode}) => {
    const userId = useProfileStore(state => state.profile?.userId);
    useNotificationWebSocket(userId || null);

    return (<>{children}</>)
}