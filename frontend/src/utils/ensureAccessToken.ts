import { API_BASE } from "@/constants/routes";
import { getMe } from "@/services/authServices";
import { useAuthStore } from "@/store/useAuthStore";
import { useProfileStore } from "@/store/useProfileStore";
import axios from "axios";

export async function ensureAccessToken(): Promise<string | null> {
  const token = useAuthStore.getState().token;
  const setToken = useAuthStore.getState().setToken;
  const clearToken = useAuthStore.getState().clearToken;

  if (token) return token;

  try {
    const res = await axios.post(
      `${API_BASE}/auth/refresh`,
      {},
      {
        withCredentials: true,
      }
    );
    console.log(res);
    const newToken = res.data?.accessToken;
    if (newToken) {
      setToken(newToken);
      const authData = await getMe();
      useProfileStore.getState().setProfile(authData);
      return newToken;
    }
  } catch (err) {
    console.error("Token refresh failed", err);
    clearToken();
  }
  return null;
}