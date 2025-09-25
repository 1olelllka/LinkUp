import { create } from "zustand";
import { persist } from "zustand/middleware";


export type ZustandProfile = {
  userId: string;
  alias: string;
  email: string;
};

type ProfileState = {
  profile: ZustandProfile | null;
  setProfile: (data: ZustandProfile) => void;
  clearProfile: () => void;
};

export const useProfileStore = create<ProfileState>()(
  persist(
    (set) => ({
      profile: null,
      setProfile: (data) => set({ profile: data }),
      clearProfile: () => set({ profile: null }),
    }),
    {
      name: "profile",
      storage: {
        getItem: (name) => {
          const item = sessionStorage.getItem(name);
          return item ? JSON.parse(item) : null;
        },
        setItem: (name, value) => {
          sessionStorage.setItem(name, JSON.stringify(value));
        },
        removeItem: (name) => {
          sessionStorage.removeItem(name);
        },
      },
    }
  )
);