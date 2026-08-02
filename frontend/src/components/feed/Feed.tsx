import { PostCard } from "@/components/posts/PostCard";
import { useFeed } from "@/hooks/useFeed";
import { useCallback } from "react";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import { Pin } from "lucide-react";

export const Feed = ({ userId }: { userId: string }) => {
  const { posts, postPage, loading, loadMoreFeeds, error } = useFeed(userId);

  const handleLoadingMoreFeeds = useCallback(async () => {
    if (!userId) return;
    await loadMoreFeeds();
  }, [loadMoreFeeds, userId]);

  return (
    <div className="max-w-2xl mx-auto px-2 md:px-0">
      {/* Header */}
      <div className="flex items-start justify-between mt-6 mb-6">
        <div className="flex items-start gap-3">
          <Pin
            className="w-5 h-5 mt-1 rotate-[-12deg] drop-shadow"
            style={{ color: "#D9A441" }}
            fill="#D9A441"
          />
          <div>
            <h2 className="font-display text-2xl font-bold text-[#F3EBD9]">Feed</h2>
            <p className="font-hand text-lg text-[#CBBFA0] mt-0.5">what your people are up to</p>
          </div>
        </div>

        {posts.length > 0 && (
          <span className="hidden sm:block font-display text-xs text-[#8A7F6C] border border-[#C9A063] bg-[#F3EBD9] px-3 py-1 rounded-sm shadow-sm">
            {posts.length} {posts.length === 1 ? "post" : "posts"}
          </span>
        )}
      </div>

      {error ? (
        <ServiceError err={error} />
      ) : (
        <>
          {posts.length > 0 ? (
            <div className="space-y-4">
              {posts.map((item) => (
                <PostCard key={item.id} {...item} />
              ))}
            </div>
          ) : (
            !loading && (
              <div className="flex flex-col items-center justify-center py-16 bg-[#E8DFC8] border border-[#C9A063] rounded-sm shadow-lg">
                <Pin
                  className="w-8 h-8 mb-3 rotate-[-15deg]"
                  style={{ color: "#D9A441" }}
                  fill="#D9A441"
                />
                <p className="font-hand text-xl text-[#8A7F6C]">nothing pinned here yet</p>
                <p className="text-sm text-[#8A7F6C] mt-1">follow a few people to fill your feed</p>
              </div>
            )
          )}

          {loading && <PageLoader />}

          {postPage && !postPage.last && (
            <div className="mt-6">
              {!loading && (
                <p
                  className="text-center font-semibold text-sm text-[#D9A441] hover:underline cursor-pointer transition-colors"
                  onClick={handleLoadingMoreFeeds}
                >
                  🚀 Load More
                </p>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};