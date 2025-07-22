import { getPostsForSpecificUser } from "@/services/postServices";
import type { Post } from "@/types/Post";
import { useEffect, useState } from "react"

export const useUserPosts = (userId: string) => {
    const [posts, setPosts] = useState<Post[]>();

    useEffect(() => {
        getPostsForSpecificUser(userId)
        .then(setPosts)
        .catch(err => console.log(err));
    }, [userId])

    return posts;
}