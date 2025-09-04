import { getFolloweesForSpecificProfile, getFollowersForSpecificProfile } from "@/services/profileServices";
import type { ProfilePage } from "@/types/Profile";
import { AxiosError } from "axios";
import { useEffect, useState } from "react"
import { useSearchParams } from "react-router";


export const useFollowList = (data: {userId: string | undefined, type: "followee" | "follower"}) => {
    const [followListPage, setFollowListPage] = useState<ProfilePage>();
    const searchParams = useSearchParams()
    const pageNumber = searchParams[0].get("page");
    const [error, setError] = useState<AxiosError | null>(null);

    useEffect(() => {
        if (data.userId) {
            if (data.type === "followee") {
                getFolloweesForSpecificProfile(data.userId, pageNumber)
                .then(setFollowListPage)
                .catch(setError);
            } else {
                getFollowersForSpecificProfile(data.userId, pageNumber)
                .then(setFollowListPage)
                .catch(setError);
            }
        }
    }, [data.userId, pageNumber, data.type]);

    return {followListPage, error}
}