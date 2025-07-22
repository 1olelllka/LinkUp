import { getSpecificProfileInfo } from "@/services/profileServices";
import { type Profile } from "@/types/Profile";
import { useEffect, useState } from "react"


export const useProfileDetail = (userId : string) => {
    const [profile, setProfile] = useState<Profile>();

    useEffect(() => {
        getSpecificProfileInfo(userId)
        .then(setProfile)
        .catch(err => console.log(err));
    }, [userId]);

    return profile;
}