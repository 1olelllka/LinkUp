import { getAllNotificationsForUser, updateAllNotificationStatuses } from "@/services/notificationService";
import type { Notification, NotificationPage } from "@/types/Notification";
import { AxiosError } from "axios";
import { useEffect, useRef, useState } from "react"
import { toast } from "sonner";


export const useNotifications = (userId : string) => {
    const [notificationPage, setNotificationPage] = useState<NotificationPage | null>();
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);
    const pendingReadIds = useRef<Set<string>>(new Set());
    const [error, setError] = useState<AxiosError>();

    useEffect(() => {
        if (!userId) return;
        setLoading(true);
            getAllNotificationsForUser(userId, 0)
            .then(response => {
                setNotificationPage(response);
                setNotifications(response.content);
                setCurrentPage(0);
            })
            .catch(err => setError(err as AxiosError))
            .finally(() => setLoading(false));
    }, [userId]);

    useEffect(() => {
        const interval = setInterval(() => {
        if (pendingReadIds.current.size > 0) {
            updateAllNotificationStatuses([...pendingReadIds.current])
            .catch(err => toast.error("Error while upding notification's read status. " + (err as AxiosError).message));
            pendingReadIds.current.clear();
        }
        }, 2000);
        return () => clearInterval(interval);
    }, []);

    const markAsRead = (id: string) => {
        setTimeout(() => {
            setNotifications((prev) =>
                prev.map((n) => (n.id === id ? { ...n, read: true } : n))
            );
            pendingReadIds.current.add(id);
        }, 500)
    };

    const loadNewNotifications = async () => {
        if (loading || !userId) return;
        setLoading(true);
        try {
            const res = await getAllNotificationsForUser(userId, currentPage + 1);
            setCurrentPage(currentPage + 1);
            setNotificationPage(res);
            setNotifications((prev) => [...prev, ...res.content]);
        } catch (err) {
            setError(err as AxiosError)
        } finally {
            setLoading(false);
        }
    }

    return {
        notifications, 
        notificationPage, 
        loading, 
        loadNewNotifications, 
        setNotifications, 
        setNotificationPage, 
        markAsRead,
        error
    };
}