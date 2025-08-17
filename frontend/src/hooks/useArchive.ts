import { getAllStoriesForArchiveByUser } from "@/services/storyServices";
import type { Story, StoryPage } from "@/types/Stories";
import { useEffect, useState } from "react"

export const useArchive = (userId: string | undefined) => {
    const [stories, setStories] = useState<Story[]>([]);
    const [storyPage, setStoryPage] = useState<StoryPage>();
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (!userId) return;
        getAllStoriesForArchiveByUser(userId, 0)
        .then(response => {
            setStoryPage(response);
            setStories(response.content);
            setPage(0);
        })
    }, [userId]);

    const loadMoreStoriesInArchive = async () => {
        if (loading || !userId) return;
        setLoading(true);
        try {
            const res = await getAllStoriesForArchiveByUser(userId, page + 1);
            setPage(page + 1);
            setStoryPage(res);
            setStories((prev) => [...prev, ...res.content]);
        } catch (err) {
            console.log(err);
        } finally {
            setLoading(false);
        }
    }

    return {stories, storyPage, loading, setStories, loadMoreStoriesInArchive};
}