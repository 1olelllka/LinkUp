import { useArchive } from "@/hooks/useArchive";
import { useProfileStore } from "@/store/useProfileStore";
import { useCallback, useState } from "react";
import { StoryCard } from "./StoryCard";
import { StoryDetailLightbox } from "./StoryDetailLightBox";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import { Pin } from "lucide-react";

export function StoryArchive() {
  const userId = useProfileStore.getState().profile?.id;

  const {
    stories,
    storyPage,
    loading,
    loadMoreStoriesInArchive,
    setStories,
    error,
  } = useArchive(userId);

  const [open, setOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);

  const handleLoadingMoreStories = useCallback(async () => {
    await loadMoreStoriesInArchive();
  }, [loadMoreStoriesInArchive]);

  return (
    <div className="mt-5 bg-[#E8DFC8] border border-[#C9A063] p-6 rounded-sm shadow-lg transition-all w-[99%] flex-1 min-h-0 flex flex-col">
      {error ? (
        <>
          <div className="flex items-center gap-3 mb-6">
            <Pin
              className="w-5 h-5 rotate-[-15deg] drop-shadow"
              style={{ color: "#D9A441" }}
              fill="#D9A441"
            />

            <div>
              <h2 className="font-display text-2xl font-bold text-[#241F1A]">
                Story Archive
              </h2>

              <p className="font-hand text-lg text-[#8A7F6C]">
                your old stories, still pinned here
              </p>
            </div>
          </div>

          <ServiceError err={error} />
        </>
      ) : (
        <>
          {/* Header */}
          <div className="flex items-start justify-between mb-6">
            <div className="flex items-start gap-3">
              <Pin
                className="w-5 h-5 mt-1 rotate-[-12deg] drop-shadow"
                style={{ color: "#D9A441" }}
                fill="#D9A441"
              />

              <div>
                <h2 className="font-display text-2xl font-bold text-[#241F1A]">
                  Story Archive
                </h2>

                <p className="font-hand text-lg text-[#8A7F6C] mt-0.5">
                  all your available & unavailable stories
                </p>
              </div>
            </div>

            {stories && stories.length > 0 && (
              <span className="hidden sm:block font-display text-xs text-[#8A7F6C] border border-[#C9A063] bg-[#F3EBD9] px-3 py-1 rounded-sm">
                {stories.length}{" "}
                {stories.length === 1 ? "story" : "stories"}
              </span>
            )}
          </div>

          {/* Stories */}
          {stories && stories.length > 0 ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-5 pt-2">
              {stories.map((story, idx) => (
                <div
                  key={story.id ?? idx}
                  className={`
                    relative
                    bg-[#F3EBD9]
                    border
                    border-[#C9A063]
                    rounded-sm
                    shadow-md
                    hover:shadow-xl
                    transition-all
                    duration-300
                    hover:-translate-y-1
                    ${
                      [
                        "-rotate-1",
                        "rotate-1",
                        "rotate-2",
                        "-rotate-2",
                      ][idx % 4]
                    }
                  `}
                >
                  {/* Pin */}
                  <Pin
                    className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 w-4 h-4 z-20 drop-shadow rotate-[-10deg]"
                    style={{ color: "#D9A441" }}
                    fill="#D9A441"
                  />

                  <StoryCard
                    story={story}
                    setStories={setStories}
                    onClickImage={() => {
                      setSelectedIndex(idx);
                      setOpen(true);
                    }}
                  />
                </div>
              ))}
            </div>
          ) : (
            !loading && (
              <div className="flex flex-col items-center justify-center py-16">
                <Pin
                  className="w-8 h-8 mb-3 rotate-[-15deg]"
                  style={{ color: "#D9A441" }}
                  fill="#D9A441"
                />

                <p className="font-hand text-xl text-[#8A7F6C]">
                  nothing pinned here yet
                </p>

                <p className="text-sm text-[#8A7F6C] mt-1">
                  Your archived stories will appear here.
                </p>
              </div>
            )
          )}

          {/* Loading */}
          {loading && <PageLoader />}

          {/* Load more */}
          {storyPage && storyPage.last !== true && (
            <div className="mt-6">
              {!loading && (
                <p
                  className="
                    text-center
                    font-semibold
                    text-sm
                    text-[#B23A2E]
                    hover:underline
                    cursor-pointer
                    transition-colors
                  "
                  onClick={handleLoadingMoreStories}
                >
                  🚀 Load More
                </p>
              )}
            </div>
          )}

          {/* Lightbox */}
          <StoryDetailLightbox
            storyPage={storyPage}
            stories={stories}
            open={open}
            setOpen={setOpen}
            selectedIndex={selectedIndex}
            handleLoadingMoreStories={handleLoadingMoreStories}
          />
        </>
      )}
    </div>
  );
}