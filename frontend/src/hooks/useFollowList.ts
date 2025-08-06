import { getFolloweesForSpecificProfile, getFollowersForSpecificProfile } from "@/services/profileServices";
import type { ProfilePage } from "@/types/Profile";
import { useEffect, useState } from "react"


export const useFollowList = (data: {userId: string | undefined, pageNumber: number | 0, type: "followee" | "follower"}) => {
    const [followListPage, setFollowListPage] = useState<ProfilePage>();

    useEffect(() => {
        if (data.userId) {
            if (data.type === "followee") {
                getFolloweesForSpecificProfile(data.userId, data.pageNumber)
                .then(setFollowListPage)
                .catch(err => console.log(err));
            } else {
                getFollowersForSpecificProfile(data.userId, data.pageNumber)
                .then(setFollowListPage)
                .catch((err) => console.log(err));
            }
        }
    }, [data.userId, data.pageNumber, data.type]);

    return followListPage
}