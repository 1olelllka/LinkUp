import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import type { Story, StoryPage } from "@/types/Stories";
import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation, Pagination, Keyboard } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import type { Swiper as Sw } from "swiper/types";
import { ChevronLeft, ChevronRight, Keyboard as KeyboardIcon } from "lucide-react";

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
  const handleSlideChange = (swiper: Sw) => {
    if (swiper.isEnd && !storyPage?.last) {
      handleLoadingMoreStories();
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent
        className="
          w-[95vw]
          max-w-[1200px]
          h-[90vh]
          p-0
          bg-black/95
          border-none
          rounded-xl
          overflow-hidden
          shadow-2xl
        "
      >
        <DialogTitle className="sr-only">
          Story viewer
        </DialogTitle>

        {stories && stories.length > 0 && (
          <>
            <Swiper
              initialSlide={selectedIndex}
              navigation={{
                enabled: true,
              }}
              pagination={{
                clickable: true,
                dynamicBullets: true,
              }}
              keyboard={{
                enabled: true,
                onlyInViewport: true,
              }}
              modules={[Navigation, Pagination, Keyboard]}
              className="w-full h-full story-swiper"
              spaceBetween={0}
              slidesPerView={1}
              loop={false}
              onSlideChange={handleSlideChange}
            >
              {stories.map((story, idx) => (
                <SwiperSlide
                  key={story.id || idx}
                  className="!flex items-center justify-center"
                >
                  <div className="relative flex items-center justify-center w-full h-full p-8">
                    {/* Image */}
                    <img
                      src={story.image}
                      alt={`Story ${idx + 1}`}
                      className="
                        block
                        max-w-full
                        max-h-full
                        w-auto
                        h-auto
                        object-contain
                        rounded-md
                        select-none
                      "
                      loading="lazy"
                    />

                    {/* Unavailable badge */}
                    {!story.available && (
                      <div className="absolute top-6 left-1/2 -translate-x-1/2">
                        <span className="
                          px-3 py-1.5
                          rounded-full
                          bg-black/70
                          backdrop-blur-sm
                          text-white
                          text-xs
                          font-medium
                          border border-white/10
                        ">
                          Unavailable
                        </span>
                      </div>
                    )}

                    {/* Date */}
                    <div className="absolute bottom-7 left-1/2 -translate-x-1/2">
                      <span className="
                        px-3 py-1
                        rounded-full
                        bg-black/60
                        backdrop-blur-sm
                        text-white/80
                        text-xs
                      ">
                        {new Date(story.createdAt).toLocaleDateString()}
                      </span>
                    </div>
                  </div>
                </SwiperSlide>
              ))}
            </Swiper>

            {/* Left navigation hint */}
            <div className="
              absolute
              left-5
              top-1/2
              -translate-y-1/2
              z-20
              pointer-events-none
              hidden sm:flex
              items-center gap-2
              text-white/50
              text-xs
            ">
              <ChevronLeft size={18} />
              <span>Previous</span>
            </div>

            {/* Right navigation hint */}
            <div className="
              absolute
              right-5
              top-1/2
              -translate-y-1/2
              z-20
              pointer-events-none
              hidden sm:flex
              items-center gap-2
              text-white/50
              text-xs
            ">
              <span>Next</span>
              <ChevronRight size={18} />
            </div>

            {/* Keyboard hint */}
            <div className="
              absolute
              top-5
              left-1/2
              -translate-x-1/2
              z-20
              hidden md:flex
              items-center gap-2
              px-3 py-1.5
              rounded-full
              bg-black/50
              backdrop-blur-sm
              text-white/50
              text-xs
              pointer-events-none
            ">
              <KeyboardIcon size={14} />
              <span>Use ← → to navigate</span>
            </div>

            {/* Counter */}
            <div className="
              absolute
              top-5
              left-5
              z-20
              px-3 py-1.5
              rounded-full
              bg-black/50
              backdrop-blur-sm
              text-white/60
              text-xs
              pointer-events-none
            ">
              {selectedIndex + 1} / {stories.length}
            </div>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
};