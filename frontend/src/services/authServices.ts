import axios from "axios"
import { API_ROUTES } from "@/constants/routes"
import axiosInterceptor from "@/lib/api/axios";

export async function login(data: {email: string, password: string}) {
    const res = await axios.post(API_ROUTES.auth.login, data, {
        withCredentials: true
    });
    return res.data;
}

export async function register(data: {
    email: string,
    alias: string,
    password: string,
    name: string
    gender: "MALE" | "FEMALE" | "UNDEFINED",
    dateOfBirth: string
}) {
    const res = await axios.post(API_ROUTES.auth.signup, data);
    return res.data;
}

export async function getMe() {
    const res = await axiosInterceptor.get(`${API_ROUTES.auth.me}`);
    return res.data;
}

export async function patchMe(data: {email : string, alias: string}) {
    const res = await axiosInterceptor.patch(`${API_ROUTES.auth.me}`, data);
    return res.data;
}

export async function logout() {
    const res = await axiosInterceptor.post(`${API_ROUTES.auth.logout}`);
    return res;
}