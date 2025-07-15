import { PostCard } from "@/components/PostCard";
import axios from "axios";
import { useEffect, useState } from "react";

export type Post = {
    id: number,
    user_id: string,
    desc: string,
    image: string,
    created_at: string
}

export const Feed = () => {
    const [posts, setPosts] = useState<Post[]>([]);

    useEffect(() => {
        axios.get("http://localhost:8080/api/feeds/436c5a79-ee35-4995-86d1-475e3a14d584", {
            headers: {
                "Authorization": "Bearer eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJMaW5rVXAiLCJzdWIiOiI0MzZjNWE3OS1lZTM1LTQ5OTUtODZkMS00NzVlM2ExNGQ1ODQiLCJpYXQiOjE3NTI2MDE1OTEsImV4cCI6MTc1MjYwNTE5MX0.uCOPnpLhuLqE2-Jo1IcxI5XeRTwZ0CtM9PxDpRbHRUrDMPWZxHodNlovl2CKTGsJ"
            }
        })
        .then((response) => {
            setPosts(response.data.content);
        })
        .catch((err) => {
            console.log(err);
        })
    }, []);

    return (
        <>
        {posts.map((item) => (
            <div className="space-y-4">
                <PostCard
                    id={item.id}
                    user_id={item.user_id}
                    desc={item.desc}
                    image={item.image}
                    created_at={item.created_at}
                />
        </div>
        ))}
        </>
    );
}