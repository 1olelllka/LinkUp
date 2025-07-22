import { PostCard } from "@/components/posts/PostCard";
import { useFeed } from "@/hooks/useFeed";

export const Feed = () => {
  // const userId = localStorage.getItem("userId") || "fallback-id"; // or get from auth store
  const userId = "436c5a79-ee35-4995-86d1-475e3a14d584";
  const { posts } = useFeed(userId);

  return (
    <div className="space-y-4">
      {posts?.map((item) => (
        <PostCard key={item.id} {...item} />
      ))}
    </div>
  );
};