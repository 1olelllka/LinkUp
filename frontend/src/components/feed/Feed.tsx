import { PostCard } from "@/components/posts/PostCard";
import { useFeed } from "@/hooks/useFeed";

export const Feed = ({userId}: {userId: string}) => {
  const { posts } = useFeed(userId);

  return (
    <div className="space-y-4">
      {posts?.map((item) => (
        <PostCard key={item.id} {...item} />
      ))}
    </div>
  );
};