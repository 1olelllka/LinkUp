import { API_ROUTES } from "@/constants/routes"
import axiosInterceptor from "@/lib/api/axios";
import axios from "axios"


export const getPostsForSpecificUser = async (userId: string, pageNumber : number) => {
    const res = await axios.get(`${API_ROUTES.posts.list}${userId}?page=${pageNumber}`);
    return res.data;
}

export const getPostDetailsById = async (id: number) => {
    const res = await axios.get(`${API_ROUTES.posts.detail(id)}`);
    return res.data;
}

export const createNewPost = async (userId: string | undefined, data: {image: string, desc: string}) => {
    if (!userId) return;
    const res = await axiosInterceptor.post(`${API_ROUTES.posts.list}/${userId}`, data);
    return res;
}

export const updatePost = async (id: number | undefined, data: {image : string, desc: string}) => {
    if (!id) return;
    const res = await axiosInterceptor.patch(`${API_ROUTES.posts.detail(id)}`, data);
    return res;
}

export const deletePostById = async (id: number) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.posts.detail(id)}`);
    return res;
}

export const createNewCommentForSpecificPost = async (data: {post: number, text: string, parent?: number}) => {
    const res = await axiosInterceptor.post(`${API_ROUTES.comments.create(data.post)}`, data);
    return res.data;
}

export const getAllCommentsForSpecificPost = async (id: number, pageNumber: number) => {
    const res = await axios.get(`${API_ROUTES.comments.list(id)}?page=${pageNumber}`);
    return res.data;
}

export const deleteSpecificComment = async (id: number) => {
    const res = await axiosInterceptor.delete(`${API_ROUTES.comments.delete(id)}`);
    return res.status
}