import { StoryDetailLightbox } from "@/components/story/StoryDetailLightBox";
import { Label } from "@/components/ui/label"
import {
  Sidebar,
  SidebarContent,
} from "@/components/ui/sidebar"
import { useStories } from "@/hooks/useStories"
import { useProfileStore } from "@/store/useProfileStore"
import { useEffect, useRef, useState } from "react";

export function RightSidebar() {

  const userId = useProfileStore.getState().profile?.userId;
  const {stories, storyPage, loadMoreStories} = useStories(userId);
  const [open, setOpen] = useState(false)
  const [selectedIndex, setSelectedIndex] = useState(0)
  const scrollRef = useRef<HTMLDivElement>(null)


  useEffect(() => {
    const container = scrollRef.current
    if (!container) return

    const handleScroll = async () => {
      const { scrollLeft, scrollWidth, clientWidth } = container
      // near the right edge + not currently loading + more pages exist
      if (
        scrollLeft + clientWidth >= scrollWidth - 100 &&
        storyPage &&
        !storyPage.last) {
          await loadMoreStories();
      }
    }

    container.addEventListener("scroll", handleScroll)
    return () => container.removeEventListener("scroll", handleScroll)
  }, [scrollRef, storyPage, loadMoreStories])


  return (
    <Sidebar collapsible="none" variant="floating" side="right" className="bg-slate-200 rounded-xl max-h-[calc(100vh-60vh)] m-2">
      <SidebarContent className="p-3 rounded-xl">
        <Label className="text-2xl font-semibold">Stories</Label>
        <div ref={scrollRef} className="flex space-x-2 overflow-x-auto">
          {stories?.map((story, idx) => (
            <div
            className="flex-none"
            onClick={() => {
              setSelectedIndex(idx)
              setOpen(true);
            }}
          >
            <img 
              src={story.image} 
              className="w-25 h-40 rounded-xl object-cover hover:cursor-pointer hover:opacity-75" 
            />
          </div>
          ))}
        </div>
        <StoryDetailLightbox
          stories={stories}
          open={open}
          setOpen={setOpen}
          selectedIndex={selectedIndex}
        />
      </SidebarContent>
    </Sidebar>
  )
}