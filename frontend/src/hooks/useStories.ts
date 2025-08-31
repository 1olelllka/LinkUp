import { getAllStoriesForUser } from "@/services/storyServices";
import type { Story, StoryPage } from "@/types/Stories";
import { AxiosError } from "axios";
import { useEffect, useState } from "react"


export const useStories = (userId : string | undefined) => {
    const [stories, setStories] = useState<Story[]>([]);
    const [storyPage, setStoryPage] = useState<StoryPage>();
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [error, setError] = useState<AxiosError>();

    useEffect(() => {
        if (!userId) return;
        getAllStoriesForUser(userId, 0)
        .then((response) => {
            setStoryPage(response);
            setStories(response.content);
            setPage(0);
        }).catch((err) => setError(err as AxiosError));
    }, [userId])

    const loadMoreStories = async () => {
        if (loading || !userId) return;
        setLoading(true);
        try {
            const res = await getAllStoriesForUser(userId, page + 1);
            setStoryPage(res);
            setStories((prev) => [...prev, ...res.content]);
            setPage(page + 1);
        } catch (err) {
            setError(err as AxiosError)
        } finally {
            setLoading(false);
        }
    }

    return {stories, storyPage, loadMoreStories, error};
}