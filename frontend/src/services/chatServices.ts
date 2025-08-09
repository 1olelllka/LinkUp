import { API_ROUTES } from "@/constants/routes";
import axiosInterceptor from "@/lib/api/axios"


export const fetchChatList = async (userId: string, pageNumber: number) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.chats.list}${userId}?page=${pageNumber}`);
    return res.data;
}

export const fetchMessagesList = async (chatId: string, pageNumber: number) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.messages.list}${chatId}/messages?page=${pageNumber}`);
    return res.data;
}

export const updateMessageById = async (id: string, data: {content: string}) => {
    const res = await axiosInterceptor.patch(`${API_ROUTES.messages.details(id)}`, data);
    return res.data;
}

export const deleteSpecificMessageById = async (msgId: string) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.messages.details(msgId)}`);
    return res;
}

export const deleteChatById = async (id: string) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.chats.detail(id)}`);
    return res;
}