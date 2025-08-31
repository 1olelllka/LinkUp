import {create} from "zustand"

type HealthState = {
    down: boolean,
    setDown: (health: boolean) => void;
}

export const useHealthStore = create<HealthState>((set) => ({
    down: false,
    setDown: (val) => set({down: val})
}));