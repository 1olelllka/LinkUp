import { getPostsForSpecificUser } from "@/services/postServices";
import type { Post, PostPage } from "@/types/Post";
import { useEffect, useState } from "react"

export const useUserPosts = (userId: string | undefined) => {
    const [postPage, setPostPage] = useState<PostPage>();
    const [posts, setPosts] = useState<Post[]>([]);
    const [pageNumber, setPageNumber] = useState(1);
    const [loading, setLoading] = useState<boolean>(false);

    useEffect(() => {
        if (userId) {
            getPostsForSpecificUser(userId, 1)
            .then(response => {
                setPostPage(response);
                setPosts(response.results)
                setPageNumber(1)
             })
            .catch(err => console.log(err));
        }
    }, [userId])

    const loadMorePosts = async () => {
        if (loading || !userId) return;
        setLoading(true);
        try {
            const res = await getPostsForSpecificUser(userId, pageNumber + 1);
            setPosts((prev) => [...res.results, ...prev].sort((a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime()));
            setPostPage(res);
            setPageNumber(pageNumber + 1)
        } catch (err) {
            console.log(err);
        } finally {
            setLoading(false);
        }
    }

    return {posts, setPosts, postPage, loadMorePosts, loading, setLoading};
}