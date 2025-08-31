
import { Card, CardContent } from "@/components/ui/card"
import { DiamondPlus, Eraser } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { deleteSpecificStory } from "@/services/storyServices"
import type { Story } from "@/types/Stories"
import { UpdateStoryDialog } from "./UpdateStoryDialog"
import { toast } from "sonner"
import type { AxiosError } from "axios"

export const StoryCard = ({
  story,
  onClickImage,
  setStories,
}: {
  story: Story
  onClickImage: () => void
  setStories: React.Dispatch<React.SetStateAction<Story[]>>
}) => {
  return (
    <Card className="overflow-hidden hover:shadow-lg transition group rounded-2xl">
      <div
        className="relative w-full h-48 cursor-pointer"
        onClick={onClickImage}
      >
        <img
          src={story.image}
          className={`w-full h-full object-cover ${
            story.available ? "" : "grayscale opacity-60"
          } group-hover:scale-105 transition duration-300`}
        />
      </div>

      {/* Info + delete dialog */}
      <CardContent className="h-9 p-2 text-xs text-muted-foreground border-t">
        <div className="flex justify-between items-center">
          <span>{new Date(story.createdAt).toLocaleDateString()}</span>
          <span
            className={`px-2 py-0.5 rounded-full text-[10px] font-medium ${
              story.available
                ? "bg-green-100 text-green-600"
                : "bg-gray-200 text-gray-600"
            }`}
          >
            {story.available ? "Visible for others" : "Not visible for others"}
          </span>
        </div>
        <div className="flex justify-end">
          <UpdateStoryDialog trigger={
              <DiamondPlus className="hover:text-blue-400 cursor-pointer" size={20}/>
          } imageUrl={story.image} id={story.id}/>
          <Dialog>
            <DialogTrigger asChild>
              <span className="p-1">
                <Eraser
                  className="hover:text-red-400 cursor-pointer"
                  size={20}
                />
              </span>
            </DialogTrigger>
            <DialogContent className="w-100">
              <DialogHeader>
                <DialogTitle>Are you sure?</DialogTitle>
                <DialogDescription>
                  After deletion the story cannot be restored
                </DialogDescription>
              </DialogHeader>
              <DialogFooter>
                <DialogClose asChild>
                  <Button variant="outline">Cancel</Button>
                </DialogClose>
                <Button
                  variant="destructive"
                  onClick={async () => {
                    deleteSpecificStory(story.id)
                      .then((response) => {
                        if (response.status == 204) {
                          setStories((prev) =>
                            prev.filter((s) => s.id != story.id)
                          )
                        } else {
                          toast.warning("Unexpected response. " + response.data);
                        }
                      })
                      .catch((err) => {
                        const error = err as AxiosError;
                        if (error.response && error.response.status == 401) {
                          toast.error("Error while deleting the story. " + (error.response.data as {message: string}).message);
                        } else {
                          toast.error(error.message);
                        }
                      })
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
  )
}