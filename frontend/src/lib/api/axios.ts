import { API_BASE } from "@/constants/routes"
import axios from "axios"

const axiosInterceptor = axios.create({
    baseURL: API_BASE,
    withCredentials: false,
    headers: {
        "Content-Type" : "application/json"
    }
});

axiosInterceptor.interceptors.request.use((config) => {
    // const token = localStorage.getItem("token");
    // if (token) {
    //     config.headers.Authorization = `Bearer ${token}`;
    // }
    // config.headers.Authorization = "Bearer eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJMaW5rVXAiLCJzdWIiOiIyZTRmMDI4ZS1mZTlhLTQxM2ItOGY3MS01YzFmZjY0OTdhYzgiLCJpYXQiOjE3NTMxMjg2MDEsImV4cCI6MTc1MzEzMjIwMX0.FsnXi-GzuC_FLFNQbahHwXMI1k8MDVWhoBQkYAmAQM9YDx9JOvzuPog_E6XUgATC";
    config.headers.Authorization = "Bearer eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJMaW5rVXAiLCJzdWIiOiI0MzZjNWE3OS1lZTM1LTQ5OTUtODZkMS00NzVlM2ExNGQ1ODQiLCJpYXQiOjE3NTMxODc1MDMsImV4cCI6MTc1MzE5MTEwM30.b18TcAc8hHXV_uRXIHFrwi75FfnQpMUxbnRqNnMg52GroHXsnC86Dtda1wCaa5WP";
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