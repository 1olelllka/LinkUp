import { getMe } from "@/services/authServices";
import { getSpecificProfileInfo } from "@/services/profileServices";
import type { Profile } from "@/types/Profile";
import { AxiosError } from "axios";
import { useEffect, useState } from "react";


export const useMyProfileDetail = () => {
    const [profile, setProfile] = useState<Profile>();
    const [error, setError] = useState<AxiosError>();
    const [loading, setLoading] = useState(false);
    
    useEffect(() => {
    setLoading(true);
    // setTimeout(() => {
        getMe()
        .then((response) => {
            const user = response;
            setProfile(user);
            getSpecificProfileInfo(user.userId)
            .then((response) => {
                const combinedProfile = {
                    ...user,
                    ...response
                };
                setProfile(combinedProfile);
            }).catch(err => setError(err as AxiosError));
        }).catch(err => setError(err as AxiosError))
        .finally(() => setLoading(false));
    // }, 2500)
  }, []);

  return {profile, setProfile, error, loading};
}