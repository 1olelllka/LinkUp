import { getPostDetailsById } from "@/services/postServices";
import type { Post } from "@/types/Post";
import { useEffect, useState } from "react"


export const usePostDetails = (id: number) => {
    const [post, setPost] = useState<Post>();

    useEffect(() => {
        if (id) {
            getPostDetailsById(id)
            .then(setPost)
            .catch(err => console.log(err));
        }
    }, [id])

    return post;
}