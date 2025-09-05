import { getSpecificProfileInfo } from "@/services/profileServices";
import { type Profile } from "@/types/Profile";
import { AxiosError } from "axios";
import { useEffect, useState } from "react"


export const useProfileDetail = (userId : string | undefined) => {
    const [profile, setProfile] = useState<Profile>();
    const [detailError, setError] = useState<AxiosError | null>(null);

    useEffect(() => {
        if (userId) {
            getSpecificProfileInfo(userId)
            .then(setProfile)
            .catch(err => setError(err));
        }
    }, [userId]);

    return {profile, detailError};
}