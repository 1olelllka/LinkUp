import { API_ROUTES } from "@/constants/routes";
import axiosInterceptor from "@/lib/api/axios";

export const fetchUserFeed = async (userId: string) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.feed.list}/${userId}`);
    return res.data.content;
}