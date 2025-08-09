import { API_ROUTES } from "@/constants/routes";
import axiosInterceptor from "@/lib/api/axios"


export const fetchChatList = async (userId: string, pageNumber: number | 0) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.chats.list}${userId}?page=${pageNumber}`);
    return res.data;
}

export const fetchMessagesList = async (chatId: string) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.messages.list}${chatId}/messages`);
    return res.data;
}

export const deleteChatById = async (id: string) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.chats.detail(id)}`);
    return res;
}