import { getMe } from "@/services/authServices";
import { getSpecificProfileInfo } from "@/services/profileServices";
import type { Profile } from "@/types/Profile";
import { useEffect, useState } from "react";


export const useMyProfileDetail = () => {
    const [profile, setProfile] = useState<Profile>();
    
    useEffect(() => {
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
        }).catch(err => console.log(err));
    }).catch(err => console.log(err));
  }, []);

  return {profile, setProfile};
}