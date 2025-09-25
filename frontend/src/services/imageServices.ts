import { API_ROUTES } from "@/constants/routes"
import axios from "axios"


export const uploadImage = async (image: File) => {
    const formData = new FormData();
    formData.append("file", image);
    const res = await axios.post(`${API_ROUTES.images.upload}`, formData, {
        headers: {
            "Content-Type": "multipart/form-data",
            "bypass-tunnel-reminder": 1
        }
    });
    return res;
}