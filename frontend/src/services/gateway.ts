import { API_ROUTES } from "@/constants/routes"
import axios from "axios"


export const checkGatewayHealthStatus = async () => {
    const res = await axios.get(`${API_ROUTES.gateway.health}`);
    return res;
}