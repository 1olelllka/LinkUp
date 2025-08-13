import { getAllNotificationsForUser, updateAllNotificationStatuses } from "@/services/notificationService";
import type { Notification, NotificationPage } from "@/types/Notification";
import { useEffect, useRef, useState } from "react"


export const useNotifications = (userId : string) => {
    const [notificationPage, setNotificationPage] = useState<NotificationPage | null>();
    const [notifications, setNotifications] = useState<Notification[]>([]);
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);
    const pendingReadIds = useRef<Set<string>>(new Set());

    useEffect(() => {
        if (!userId) return;
        getAllNotificationsForUser(userId, 0)
        .then(response => {
            setNotificationPage(response);
            setNotifications(response.content);
            setCurrentPage(0);
        })
        .catch(err => console.log(err));
    }, [userId]);

    useEffect(() => {
        const interval = setInterval(() => {
        if (pendingReadIds.current.size > 0) {
            updateAllNotificationStatuses([...pendingReadIds.current])
            .catch(console.error);
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
            console.log(err);
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
        markAsRead};
}