import { ServiceError } from "@/components/errors/ServiceUnavailable";
import { PageLoader } from "@/components/load/PageLoader";
import { CreateStoryDialog } from "@/components/story/CreateStoryDialog";
import { StoryDetailLightbox } from "@/components/story/StoryDetailLightBox";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Sidebar, SidebarContent } from "@/components/ui/sidebar";
import { useArchive } from "@/hooks/useArchive";
import { useStories } from "@/hooks/useStories";
import { useProfileStore } from "@/store/useProfileStore";
import { Plus } from "lucide-react";
import { useEffect, useRef, useState } from "react";

export function RightSidebar() {
  const userId = useProfileStore.getState().profile?.userId;
  const { stories, storyPage, loadMoreStories, error, loading } =
    useStories(userId);
  const { stories: archive, loading: loadingArchive, loadMoreStoriesInArchive, storyPage: archivePage } = useArchive(userId);
  const [open, setOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const scrollRef = useRef<HTMLDivElement>(null);

  const [others, setOthers] = useState(true);
  const [switching, setSwitching] = useState(false);

  useEffect(() => {
    const timeout = setTimeout(() => setSwitching(false), 200);
    return () => clearTimeout(timeout);
  }, [others]);

  useEffect(() => {
    const container = scrollRef.current;
    if (!container) return;

    const handleScroll = async () => {
      const { scrollLeft, scrollWidth, clientWidth } = container;
      if (others) {
        // near the right edge + not currently loading + more pages exist
        if (
          scrollLeft + clientWidth >= scrollWidth - 100 &&
          storyPage &&
          !storyPage.last
        ) {
          await loadMoreStories();
        }
      } else {
        if (
          scrollLeft + clientWidth >= scrollWidth - 100 &&
          archivePage &&
          !archivePage.last
        ) {
          await loadMoreStoriesInArchive();
        }
      }
    };

    container.addEventListener("scroll", handleScroll);
    return () => container.removeEventListener("scroll", handleScroll);
  }, [scrollRef, storyPage, loadMoreStories, archivePage, loadMoreStoriesInArchive, others]);

  return (
    <Sidebar
      collapsible="none"
      variant="floating"
      side="right"
      className="bg-slate-200 rounded-xl max-h-[calc(100vh-49vh)] m-2"
    >
      <SidebarContent className="p-3 rounded-xl min-h-60">
        {error ? (
          <>
            <Label className="text-2xl font-semibold">Stories</Label>
            <ServiceError err={error} variant="compact"/>
          </>
        ) : (
          <>
            <div className="flex justify-between">
              <Label className="text-2xl font-semibold">Stories</Label>
              <CreateStoryDialog
                trigger={
                  <Button
                    size={"icon"}
                    variant={"outline"}
                    className="cursor-pointer"
                  >
                    <Plus />
                  </Button>
                }
              />
            </div>
            <div className="flex flex-row">
              <Badge
                variant={others ? "default" : "outline"}
                className="cursor-pointer"
                onClick={() => {
                  setSwitching(true)
                  setOthers(true)
                }}
              >
                Others
              </Badge>
              <Badge
                variant={others ? "outline" : "default"}
                className="cursor-pointer"
                onClick={() => {
                  setSwitching(true)
                  setOthers(false)
                }}
              >
                Mine
              </Badge>
            </div>
            <div ref={scrollRef} className="flex space-x-2 overflow-x-auto">
              {(() => {
                const data = others ? stories : archive;
                const isLoading = others ? loading : loadingArchive;

                if (switching || isLoading) {
                  return (
                    <div className="flex justify-center items-center w-full min-h-40">
                      <PageLoader />
                    </div>
                  );
                }
                if (data && data.length > 0) {
                  return data.map((story, idx) => (
                    <div
                      key={idx}
                      className="flex-none"
                      onClick={() => {
                        setSelectedIndex(idx);
                        setOpen(true);
                      }}
                    >
                      <img
                        src={story.image}
                        className="w-25 h-40 rounded-xl object-cover hover:cursor-pointer hover:opacity-75"
                      />
                    </div>
                  ));
                }

                return (
                  <div className="flex flex-row w-200 min-h-40 justify-center items-center">
                    <p className="text-lg font-semibold pb-5">
                      🧦 No Stories found
                    </p>
                  </div>
                );
              })()}
            </div>
            <StoryDetailLightbox
              stories={others ? stories : archive}
              open={open}
              setOpen={setOpen}
              selectedIndex={selectedIndex}
              handleLoadingMoreStories={loadMoreStories}
            />
          </>
        )}
      </SidebarContent>
    </Sidebar>
  );
}
