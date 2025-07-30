import { API_ROUTES } from "@/constants/routes"
import axiosInterceptor from "@/lib/api/axios";
import axios from "axios"

export const searchProfile = async (searchTerm: string) => {
    const res = await axios.get(`${API_ROUTES.profile.search}${searchTerm}`);
    return res.data;
}

export const getSpecificProfileInfo = async (userId: string) => {
    const res = await axiosInterceptor.get(`${API_ROUTES.profile.profileDetail}${userId}`);
    return res.data;
}

export const patchPersonalProfileInfo = async (userId: string | undefined, data: 
                                        {name? : string, aboutMe?: string, gender?: "MALE" | "FEMALE" | "UNDEFINED", dateOfBirth?: string}) => {
    const res = await axiosInterceptor.patch(`${API_ROUTES.profile.profileDetail}${userId}`, data);
    return res.data;
}

export const getFollowersForSpecificProfile = async (userId: string, pageNumber: number) => {
    const res = await axios.get(`${API_ROUTES.profile.profileFollowers(userId, pageNumber)}`);
    return res.data;
}

export const getFolloweesForSpecificProfile = async (userId: string, pageNumber: number) => {
    const res = await axios.get(`${API_ROUTES.profile.profileFollowees(userId, pageNumber)}`);
    return res.data;
}