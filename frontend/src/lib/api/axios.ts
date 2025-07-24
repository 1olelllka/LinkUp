import { API_BASE } from "@/constants/routes"
import { ensureAccessToken } from "@/utils/ensureAccessToken";
import axios from "axios"

const axiosInterceptor = axios.create({
    baseURL: API_BASE,
    withCredentials: true,
    headers: {
        "Content-Type" : "application/json"
    }
});

axiosInterceptor.interceptors.request.use(async (config) => {
    const token = await ensureAccessToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
})


// Handle errors globally (optional)
// axiosInstance.interceptors.response.use(
//   (res) => res,
//   (err) => {
//     if (err.response?.status === 401) {
//       // auto logout or redirect
//       console.warn("Unauthorized. Redirect to login.");
//       localStorage.removeItem("token");
//     }
//     return Promise.reject(err);
//   }
// );

export default axiosInterceptor;