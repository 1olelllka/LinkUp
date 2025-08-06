import { API_ROUTES } from "@/constants/routes"
import axiosInterceptor from "@/lib/api/axios";
import axios from "axios"


export const getPostsForSpecificUser = async (userId: string) => {
    const res = await axios.get(`${API_ROUTES.posts.list}${userId}`);
    return res.data.results;
}

export const getPostDetailsById = async (id: number) => {
    const res = await axios.get(`${API_ROUTES.posts.detail(id)}`);
    return res.data;
}

export const deletePostById = async (id: number) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.posts.detail(id)}`);
    return res;
}

export const createNewCommentForSpecificPost = async (data: {post: number, text: string, parent?: number}) => {
    const res = await axiosInterceptor.post(`${API_ROUTES.comments.create(data.post)}`, data);
    return res.data;
}

export const getAllCommentsForSpecificPost = async (id: number) => {
    const res = await axios.get(`${API_ROUTES.comments.list(id)}`);
    return res.data;
}

export const deleteSpecificComment = async (id: number) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.comments.delete(id)}`);
    return res.status
}