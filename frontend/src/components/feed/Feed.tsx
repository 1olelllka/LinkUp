import { PostCard } from "@/components/posts/PostCard";
import { useFeed } from "@/hooks/useFeed";
import { useCallback } from "react";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";

export const Feed = ({userId}: {userId: string}) => {
  const { posts, postPage, loading, loadMoreFeeds, error } = useFeed(userId);

  const handleLoadingMoreFeeds = useCallback(async () => {
    if (!userId) return;
    await loadMoreFeeds();
  }, [loadMoreFeeds, userId]);

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold mx-2 my-4">Feeds</h2>
      {error 
        ? <ServiceError err={error} />
        : 
        <>
          {posts.length > 0
            ? posts.map((item) => (
              <PostCard key={item.id} {...item} />
            ))
            : 
            <div className="flex justify-center">
              <h1 className="text-lg font-bold">🏕️ No Feeds found</h1>
            </div>
          }
          {loading && <PageLoader />}
          {postPage && !postPage.last && 
            <div className="mt-2">
              {loading && <p 
              className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400"
              onClick={handleLoadingMoreFeeds}
              >🚀 Load More</p>
              }
            </div>
          }
        </>
      }
    </div>
  );
};