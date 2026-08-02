import { ServiceError } from "@/components/errors/ServiceUnavailable";
import { PageLoader } from "@/components/load/PageLoader";
import { CreateStoryDialog } from "@/components/story/CreateStoryDialog";
import { StoryDetailLightbox } from "@/components/story/StoryDetailLightBox";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Sidebar, SidebarContent } from "@/components/ui/sidebar";
import { useArchive } from "@/hooks/useArchive";
import { useStories } from "@/hooks/useStories";
import { useProfileStore } from "@/store/useProfileStore";
import { Plus } from "lucide-react";
import { useEffect, useRef, useState } from "react";

export function RightSidebar() {
  const userId = useProfileStore.getState().profile?.id;

  const {
    stories,
    storyPage,
    loadMoreStories,
    error,
    loading,
  } = useStories(userId);

  const {
    stories: archive,
    loading: loadingArchive,
    loadMoreStoriesInArchive,
    storyPage: archivePage,
  } = useArchive(userId);

  const [open, setOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [others, setOthers] = useState(true);
  const [switching, setSwitching] = useState(false);

  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const timeout = setTimeout(() => {
      setSwitching(false);
    }, 200);

    return () => clearTimeout(timeout);
  }, [others]);

  useEffect(() => {
    const container = scrollRef.current;

    if (!container) return;

    const handleScroll = async () => {
      const { scrollLeft, scrollWidth, clientWidth } = container;

      if (scrollLeft + clientWidth < scrollWidth - 100) {
        return;
      }

      if (others) {
        if (storyPage && !storyPage.last && !loading) {
          await loadMoreStories();
        }
      } else {
        if (
          archivePage &&
          !archivePage.last &&
          !loadingArchive
        ) {
          await loadMoreStoriesInArchive();
        }
      }
    };

    container.addEventListener("scroll", handleScroll);

    return () => {
      container.removeEventListener("scroll", handleScroll);
    };
  }, [
    storyPage,
    archivePage,
    loadMoreStories,
    loadMoreStoriesInArchive,
    loading,
    loadingArchive,
    others,
  ]);

  const currentStories = others ? stories : archive;
  const currentLoading = others ? loading : loadingArchive;

  const handleLoadMore = others
    ? loadMoreStories
    : loadMoreStoriesInArchive;

  return (
    <Sidebar
      collapsible="none"
      variant="floating"
      side="right"
      className="
        bg-[#E8DFC8]
        border-[#C9A063]
        rounded-sm
        shadow-lg
        max-h-[calc(100vh-49vh)]
        m-2
      "
    >
      <SidebarContent
        className="
          p-4
          rounded-sm
          overflow-hidden
        "
      >
        {error ? (
          <>
            <div className="flex items-center justify-between mb-3">
              <h2 className="
                font-display
                text-2xl
                font-bold
                text-[#241F1A]
              ">
                Stories
              </h2>
            </div>

            <ServiceError
              err={error}
              variant="compact"
            />
          </>
        ) : (
          <>
            {/* Header */}
            <div className="flex items-center justify-between mb-3">
              <div>
                <span className="
                  font-hand
                  text-lg
                  text-[#B23A2E]
                ">
                  little moments
                </span>

                <h2 className="
                  font-display
                  text-2xl
                  font-bold
                  text-[#241F1A]
                  -mt-1
                ">
                  Stories
                </h2>
              </div>

              <CreateStoryDialog
                trigger={
                  <Button
                    size="icon"
                    variant="outline"
                    className="
                      cursor-pointer
                      rounded-sm
                      border-[#6B4A32]
                      text-[#241F1A]
                      bg-[#F3EBD9]
                      hover:bg-[#B23A2E]
                      hover:text-[#F3EBD9]
                      hover:border-[#B23A2E]
                    "
                  >
                    <Plus />
                  </Button>
                }
              />
            </div>

            {/* Switch */}
            <div className="
              flex
              w-fit
              gap-1
              p-1
              mb-4
              rounded-sm
              bg-[#DDD0B0]
              border
              border-[#C9A063]
            ">
              <Badge
                variant="outline"
                className={`
                  cursor-pointer
                  rounded-sm
                  px-3
                  py-1
                  border-none
                  transition
                  ${
                    others
                      ? "bg-[#B23A2E] text-[#F3EBD9] hover:bg-[#B23A2E]"
                      : "bg-transparent text-[#6B4A32] hover:bg-[#E8DFC8]"
                  }
                `}
                onClick={() => {
                  if (!others) {
                    setSwitching(true);
                    setOthers(true);
                    setSelectedIndex(0);
                  }
                }}
              >
                Others
              </Badge>

              <Badge
                variant="outline"
                className={`
                  cursor-pointer
                  rounded-sm
                  px-3
                  py-1
                  border-none
                  transition
                  ${
                    !others
                      ? "bg-[#B23A2E] text-[#F3EBD9] hover:bg-[#B23A2E]"
                      : "bg-transparent text-[#6B4A32] hover:bg-[#E8DFC8]"
                  }
                `}
                onClick={() => {
                  if (others) {
                    setSwitching(true);
                    setOthers(false);
                    setSelectedIndex(0);
                  }
                }}
              >
                Mine
              </Badge>
            </div>

            {/* Stories */}
            <div
              ref={scrollRef}
              className="
                flex
                gap-3
                overflow-x-auto
                pb-3
                scrollbar-thin
                scrollbar-thumb-[#C9A063]
                scrollbar-track-transparent
              "
            >
              {switching || currentLoading ? (
                <div className="
                  flex
                  justify-center
                  items-center
                  w-full
                  min-h-40
                ">
                  <PageLoader />
                </div>
              ) : currentStories && currentStories.length > 0 ? (
                currentStories.map((story, idx) => (
                  <div
                    key={story.id ?? idx}
                    className="
                      flex-none
                      group
                      cursor-pointer
                    "
                    onClick={() => {
                      setSelectedIndex(idx);
                      setOpen(true);
                    }}
                  >
                    <div className="
                      relative
                      w-25
                      h-40
                      overflow-hidden
                      rounded-sm
                      border-2
                      border-[#C9A063]
                      bg-[#DDD0B0]
                      shadow-md
                      transition-all
                      duration-300
                      group-hover:-translate-y-1
                      group-hover:shadow-lg
                    ">
                      <img
                        src={story.image}
                        alt="Story"
                        className={`
                          w-full
                          h-full
                          object-cover
                          transition
                          duration-300
                          group-hover:scale-105
                          ${
                            !story.available
                              ? "grayscale opacity-60"
                              : ""
                          }
                        `}
                      />

                      {!story.available && (
                        <div className="
                          absolute
                          bottom-2
                          left-1/2
                          -translate-x-1/2
                          whitespace-nowrap
                          px-2
                          py-0.5
                          rounded-sm
                          bg-black/60
                          text-white
                          text-[9px]
                        ">
                          Unavailable
                        </div>
                      )}
                    </div>

                    <p className="
                      text-[10px]
                      text-center
                      text-[#8A7F6C]
                      mt-1
                    ">
                      {new Date(
                        story.createdAt
                      ).toLocaleDateString()}
                    </p>
                  </div>
                ))
              ) : (
                <div className="
                  flex
                  flex-col
                  w-full
                  min-h-40
                  justify-center
                  items-center
                  text-center
                ">
                  <span className="text-3xl mb-2">
                    🧦
                  </span>

                  <p className="
                    font-hand
                    text-lg
                    text-[#8A7F6C]
                  ">
                    No stories found
                  </p>
                </div>
              )}
            </div>

            {/* Scroll hint */}
            {currentStories &&
              currentStories.length > 3 && (
                <p className="
                  text-center
                  text-[10px]
                  text-[#8A7F6C]
                  mt-1
                ">
                  ← scroll to explore →
                </p>
              )}

            {/* Lightbox */}
            <StoryDetailLightbox
              stories={currentStories}
              open={open}
              setOpen={setOpen}
              selectedIndex={selectedIndex}
              storyPage={others ? storyPage : archivePage}
              handleLoadingMoreStories={handleLoadMore}
            />
          </>
        )}
      </SidebarContent>
    </Sidebar>
  );
}