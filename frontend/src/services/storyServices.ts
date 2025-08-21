import { API_ROUTES } from "@/constants/routes"
import axiosInterceptor from "@/lib/api/axios"


export const getAllStoriesForUser = async (userId: string, pageNumber: number) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.stories.list(userId)}?page=${pageNumber}`);
    return res.data;
}

export const getAllStoriesForArchiveByUser = async (userId: string, pageNumber: number) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.stories.archive(userId)}?page=${pageNumber}`);
    return res.data;
}

export const createNewStory = async (userId: string | undefined, data: {image: string}) => {
    if (!userId) return;
    const res = await axiosInterceptor.post(`${API_ROUTES.stories.list(userId)}`, data);
    return res;
}

export const updateStory = async (id : string, data: {image: string}) => {
    const res = await axiosInterceptor.patch(`${API_ROUTES.stories.detail(id)}`, data);
    return res;
}

export const deleteSpecificStory = async (id : string) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.stories.detail(id)}`);
    return res;
}