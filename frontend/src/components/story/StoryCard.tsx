import { Card, CardContent } from "@/components/ui/card";
import { Pencil, Trash2, Pin } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { deleteSpecificStory } from "@/services/storyServices";
import type { Story } from "@/types/Stories";
import { UpdateStoryDialog } from "./UpdateStoryDialog";
import { toast } from "sonner";
import type { AxiosError } from "axios";

export const StoryCard = ({
  story,
  onClickImage,
  setStories,
}: {
  story: Story;
  onClickImage: () => void;
  setStories: React.Dispatch<React.SetStateAction<Story[]>>;
}) => {
  return (
    <Card
      className="
        relative
        overflow-visible
        bg-[#F3EBD9]
        border
        border-[#C9A063]
        rounded-sm
        shadow-md
        hover:shadow-xl
        transition-all
        duration-300
        group
        p-0
      "
    >
      {/* Pin */}
      <Pin
        className="
          absolute
          top-0
          left-1/2
          -translate-x-1/2
          -translate-y-1/2
          w-4
          h-4
          z-20
          drop-shadow
          rotate-[-10deg]
        "
        style={{ color: "#D9A441" }}
        fill="#D9A441"
      />

      {/* Image */}
      <div
        className="
          relative
          w-full
          h-48
          cursor-pointer
          overflow-hidden
          rounded-t-sm
        "
        onClick={onClickImage}
      >
        <img
          src={story.image}
          alt="Story"
          className={`
            w-full
            h-full
            object-cover
            transition
            duration-500
            group-hover:scale-105
            ${
              story.available
                ? ""
                : "grayscale opacity-60"
            }
          `}
        />

        {/* Unavailable overlay */}
        {!story.available && (
          <div className="absolute inset-0 flex items-center justify-center bg-[#241F1A]/20">
            <span
              className="
                bg-[#F3EBD9]
                border
                border-[#C9A063]
                px-3
                py-1
                rounded-sm
                font-display
                text-[10px]
                font-bold
                text-[#4A4136]
                shadow-md
              "
            >
              ARCHIVED
            </span>
          </div>
        )}
      </div>

      {/* Info + actions */}
      <CardContent className="p-3 text-xs text-[#8A7F6C]">
        <div className="flex items-center justify-between gap-2">
          <span className="font-display text-[10px] text-[#8A7F6C]">
            {new Date(story.createdAt).toLocaleDateString()}
          </span>

          <span
            className={`
              px-2
              py-0.5
              rounded-sm
              border
              text-[9px]
              font-display
              font-bold
              whitespace-nowrap
              ${
                story.available
                  ? "bg-[#DDD0B0] border-[#C9A063] text-[#4A4136]"
                  : "bg-[#E8DFC8] border-[#8A7F6C] text-[#8A7F6C]"
              }
            `}
          >
            {story.available ? "VISIBLE" : "ARCHIVED"}
          </span>
        </div>

        <div className="flex justify-end items-center gap-1 mt-2 pt-2 border-t border-[#C9A063]/60">
          {/* Update */}
          <UpdateStoryDialog
            trigger={
              <button
                type="button"
                className="
                  p-1.5
                  rounded-sm
                  text-[#8A7F6C]
                  hover:text-[#B23A2E]
                  hover:bg-[#DDD0B0]
                  transition-colors
                  cursor-pointer
                "
                aria-label="Update story"
              >
                <Pencil size={16} />
              </button>
            }
            imageUrl={story.image}
            id={story.id}
          />

          {/* Delete */}
          <Dialog>
            <DialogTrigger asChild>
              <button
                type="button"
                className="
                  p-1.5
                  rounded-sm
                  text-[#8A7F6C]
                  hover:text-[#B23A2E]
                  hover:bg-[#DDD0B0]
                  transition-colors
                  cursor-pointer
                "
                aria-label="Delete story"
              >
                <Trash2 size={16} />
              </button>
            </DialogTrigger>

            <DialogContent
              className="
                sm:max-w-[425px]
                bg-[#E8DFC8]
                text-[#241F1A]
                border-[#C9A063]
                rounded-sm
                shadow-2xl
              "
            >
              <DialogHeader>
                <span className="font-hand text-xl text-[#D9A441]">
                  remove this note
                </span>

                <DialogTitle className="font-display text-2xl font-bold text-[#241F1A]">
                  Are you sure?
                </DialogTitle>

                <DialogDescription className="text-[#4A4136]">
                  After deletion, this story cannot be restored.
                </DialogDescription>
              </DialogHeader>

              <DialogFooter className="gap-2 sm:gap-2">
                <DialogClose asChild>
                  <Button
                    variant="outline"
                    className="
                      rounded-sm
                      border-[#6B4A32]
                      bg-transparent
                      text-[#241F1A]
                      hover:bg-[#DDD0B0]
                      hover:text-[#241F1A]
                    "
                  >
                    Cancel
                  </Button>
                </DialogClose>

                <Button
                  className="
                    rounded-sm
                    bg-[#B23A2E]
                    text-[#F3EBD9]
                    hover:bg-[#9c3226]
                  "
                  onClick={async () => {
                    deleteSpecificStory(story.id)
                      .then((response) => {
                        if (response.status == 204) {
                          setStories((prev) =>
                            prev.filter((s) => s.id != story.id)
                          );

                          toast.success(
                            "Successfully deleted story!"
                          );
                        } else {
                          toast.warning(
                            "Unexpected response. " +
                              response.data
                          );
                        }
                      })
                      .catch((err) => {
                        const error = err as AxiosError;

                        if (
                          error.response &&
                          error.response.status == 401
                        ) {
                          toast.error(
                            "Error while deleting the story. " +
                              (
                                error.response.data as {
                                  message: string;
                                }
                              ).message
                          );
                        } else {
                          toast.error(error.message);
                        }
                      });
                  }}
                >
                  Delete
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </CardContent>
    </Card>
  );
};