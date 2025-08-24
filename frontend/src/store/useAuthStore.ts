import {create} from "zustand"
import {persist} from "zustand/middleware"

type AuthState = {
    token: string | null,
    setToken: (token: string) => void;
    clearToken: () => void;
}

const TTL = 1000 * 60 * 60 // 1 hour

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      token: null,
      setToken: (token) => set({ token }),
      clearToken: () => set({ token: null }),
    }),
    {
      name: "auth",
      storage: {
        getItem: (name) => {
          const item = sessionStorage.getItem(name);
          if (!item) return null;
          try {
            const parsed = JSON.parse(item);
            if (parsed.timestamp && Date.now() - parsed.timestamp > TTL) {
              sessionStorage.removeItem(name);
              return null;
            }
            return parsed.data;
          } catch {
            return null;
          }
        },
        setItem: (name, value) => {
          const toSet = {
            data: value,
            timestamp: Date.now()
          }
          sessionStorage.setItem(name, JSON.stringify(toSet));
        },
        removeItem: (name) => {
          sessionStorage.removeItem(name);
        },
      },
    }
  )
);
