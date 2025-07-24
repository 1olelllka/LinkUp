import { useEffect, useState } from "react";
import { fetchUserFeed } from "@/services/feedServices";
import { type Post } from "@/types/Post";

export const useFeed = (userId: string | undefined) => {
    const [posts, setPosts] = useState<Post[]>();
    const [error, setError] = useState(null);

    useEffect(() => {
        if (userId) {
            fetchUserFeed(userId)
            .then(setPosts)
            .catch(setError)
        }
    }, [userId]);

    return { posts, error }
}