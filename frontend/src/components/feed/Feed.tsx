import { PostCard } from "@/components/posts/PostCard";
import { useFeed } from "@/hooks/useFeed";

export const Feed = ({userId}: {userId: string}) => {
  const { posts } = useFeed(userId);

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold mx-2 my-4">Feeds</h2>
      {posts?.map((item) => (
        <PostCard key={item.id} {...item} />
      ))}
    </div>
  );
};