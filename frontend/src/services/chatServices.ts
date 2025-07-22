import { API_ROUTES } from "@/constants/routes";
import axiosInterceptor from "@/lib/api/axios"


export const fetchChatList = async (userId: string) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.chats.list}${userId}`);
    return res.data;
}

export const fetchMessagesList = async (chatId: string) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.messages.list}${chatId}/messages`);
    return res.data.content;
}