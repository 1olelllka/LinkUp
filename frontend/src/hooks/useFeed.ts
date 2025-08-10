import { useEffect, useState } from "react";
import { fetchUserFeed } from "@/services/feedServices";
import type { Post, FeedPage } from "@/types/Post";

export const useFeed = (userId: string | undefined) => {
    const [posts, setPosts] = useState<Post[]>([]);
    const [error, setError] = useState(null);
    const [postPage, setPostPage] = useState<FeedPage>();
    const [loading, setLoading] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);

    useEffect(() => {
        if (userId) {
            fetchUserFeed(userId, 0)
            .then(response => {
                setPostPage(response);
                setPosts(response.content)
            })
            .catch(setError)
        }
    }, [userId]);

    const loadMoreFeeds = async () => {
        if (loading || !userId) return;
        setLoading(true);
        try {
            const res = await fetchUserFeed(userId, currentPage + 1);
            setCurrentPage(currentPage + 1);
            setPostPage(res);
            setPosts((prev) => [...prev, ...res.content]);
        } catch (err) {
            console.log(err);
        } finally {
            setLoading(false);
        }
    }

    return { posts, loading, postPage, loadMoreFeeds ,error }
}