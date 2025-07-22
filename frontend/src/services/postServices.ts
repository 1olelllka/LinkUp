import { API_ROUTES } from "@/constants/routes"
import axios from "axios"


export const getPostsForSpecificUser = async (userId: string) => {
    const res = await axios.get(`${API_ROUTES.posts.list}${userId}`);
    return res.data.results;
}