import {
  Dialog,
  DialogContent,
} from "@/components/ui/dialog"
import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "@/components/ui/carousel"
import type { Story } from "@/types/Stories"


export const StoryDetailLightbox = ({ stories, open, setOpen, selectedIndex }
    : {stories : Story[] | undefined, open: boolean, setOpen : (open : boolean) => void, selectedIndex: number}
) => {
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogContent className="max-w-3xl w-full p-0 rounded-2xl overflow-hidden shadow-xl">
        <Carousel opts={{ startIndex: selectedIndex }}>
          <CarouselContent>
            {stories?.map((story) => (
              <CarouselItem key={story.id} className="flex justify-center">
                <div className="relative w-full aspect-[9/16] max-h-[80vh] flex items-center justify-center">
                  <img
                    src={story.image}
                    className="w-full h-full object-contain"
                  />
                  {!story.available && (
                    <div className="absolute top-3 left-3 bg-red-600 text-white text-xs px-2 py-1 rounded">
                      Unavailable
                    </div>
                  )}
                  <div className="absolute bottom-3 left-3 text-xs opacity-80">
                    {new Date(story.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </CarouselItem>
            ))}
          </CarouselContent>
          <CarouselPrevious className="left-2 bg-white/20 hover:bg-white/40 text-white rounded-full" />
          <CarouselNext className="right-2 bg-white/20 hover:bg-white/40 text-white rounded-full" />
        </Carousel>
      </DialogContent>
    </Dialog>
  )
}
