import { getSpecificProfileInfo } from "@/services/profileServices";
import { type Profile } from "@/types/Profile";
import { AxiosError } from "axios";
import { useEffect, useState } from "react"


export const useProfileDetail = (userId : string | undefined) => {
    const [profile, setProfile] = useState<Profile>();
    const [detailError, setError] = useState<AxiosError | null>(null);
    const [detailLoading, setDetailLoading] = useState(false);

    useEffect(() => {
        if (userId) {
            setDetailLoading(true);
            // setTimeout(() => {
                getSpecificProfileInfo(userId)
                .then(setProfile)
                .catch(err => setError(err))
                .finally(() => setDetailLoading(false));
            // }, 2500)
        }
    }, [userId]);

    return {profile, detailError, detailLoading};
}