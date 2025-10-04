import { Dialog, DialogContent } from "@/components/ui/dialog";
import type { Story, StoryPage } from "@/types/Stories";
import { DialogTitle } from "@radix-ui/react-dialog";
// Try these import variations
import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation, Pagination, Keyboard } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import type { Swiper as Sw } from "swiper/types";

export const StoryDetailLightbox = ({
  stories,
  open,
  setOpen,
  selectedIndex,
  storyPage,
  handleLoadingMoreStories,
}: {
  stories: Story[] | undefined;
  open: boolean;
  setOpen: (open: boolean) => void;
  selectedIndex: number;
  storyPage?: StoryPage;
  handleLoadingMoreStories: () => Promise<void>;
}) => {

  const handleSlideChange = (swiper : Sw) => {
    if (swiper.isEnd && !storyPage?.last) {
      handleLoadingMoreStories();
    }
  }

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent className="max-w-3xl w-full p-0 rounded-2xl overflow-hidden shadow-xl">
        <DialogTitle />
        {stories && stories.length > 0 && (
          <Swiper
            initialSlide={selectedIndex}
            navigation={true}
            pagination={{
              clickable: true,
              dynamicBullets: true,
            }}
            keyboard={{
              enabled: true,
              onlyInViewport: true,
            }}
            modules={[Navigation, Pagination, Keyboard]}
            className="h-[80vh] w-full"
            spaceBetween={0}
            slidesPerView={1}
            loop={false}
            onSlideChange={handleSlideChange}
          >
            {stories.map((story, idx) => (
              <SwiperSlide
                key={story.id || idx}
                className="flex items-center justify-center"
              >
                <div className="relative w-full h-full flex items-center justify-center">
                  <img
                    src={story.image}
                    alt={`Story ${idx + 1}`}
                    className="max-w-full max-h-full object-contain"
                    loading="lazy"
                  />
                  {!story.available && (
                    <div className="absolute top-3 left-3 bg-red-600 text-white text-xs px-2 py-1 rounded">
                      Unavailable
                    </div>
                  )}
                  <div className="absolute bottom-3 left-3 text-white text-xs opacity-80 bg-black bg-opacity-50 px-2 py-1 rounded">
                    {new Date(story.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </SwiperSlide>
            ))}
          </Swiper>
        )}
      </DialogContent>
    </Dialog>
  );
};
