import { useArchive } from "@/hooks/useArchive"
import { useProfileStore } from "@/store/useProfileStore"
import { useCallback, useState } from "react";
import { StoryCard } from "./StoryCard";
import { StoryDetailLightbox } from "./StoryDetailLightBox";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";

export function StoryArchive() {

  const userId = useProfileStore.getState().profile?.userId;
  const {stories, storyPage, loading, loadMoreStoriesInArchive, setStories, error} = useArchive(userId);
  const [open, setOpen] = useState(false)
  const [selectedIndex, setSelectedIndex] = useState(0)


  const handleLoadingMoreStories = useCallback(async () => {
    await loadMoreStoriesInArchive();
  }, [loadMoreStoriesInArchive]);

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">Archive</h1>
          <p className="text-muted-foreground">All your available & unavailable stories</p>
        </div>
      </div>

      {error
      ? <ServiceError err={error} />
      : 
      <>
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
          {stories?.map((story, idx) => (
            <StoryCard
              key={story.id}
              story={story}
              setStories={setStories}
              onClickImage={() => {
                setSelectedIndex(idx)
                setOpen(true)
              }}
            />
          ))}
        </div>
        {loading && <PageLoader />}
        {storyPage && storyPage.last != true && 
          <div className="mt-2">
            {!loading && 
            <p 
            className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400"
            onClick={handleLoadingMoreStories}
            >🚀 Load More</p>
          }
          </div>
        }
        <StoryDetailLightbox
          stories={stories}
          open={open}
          setOpen={setOpen}
          selectedIndex={selectedIndex}
        />
      </>
    }
    </div>
  )
}
