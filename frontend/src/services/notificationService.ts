import { API_ROUTES } from "@/constants/routes"
import axiosInterceptor from "@/lib/api/axios"


export const getAllNotificationsForUser = async (userId: string, pageNumber : number) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.notifications.list(userId)}?page=${pageNumber}`);
    return res.data;
}

export const updateAllNotificationStatuses = async (ids : string[]) => {
    const res = await axiosInterceptor.patch(`${API_ROUTES.notifications.update_status(ids)}`);
    return res;
}

export const deleteSpecificNotification = async (id: string) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.notifications.delete(id)}`);
    return res;
}

export const deleteAllNotificationsForUser = async (userId: string) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.notifications.list(userId)}`);
    return res;
}