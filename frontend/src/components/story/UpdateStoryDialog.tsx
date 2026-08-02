import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogTitle,
  DialogTrigger,
  DialogHeader,
  DialogDescription,
} from "../ui/dialog";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { uploadImage } from "@/services/imageServices";
import { updateStory } from "@/services/storyServices";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { SubmitLoader } from "../load/SubmitLoader";
import { Pin } from "lucide-react";

export const UpdateStoryDialog = ({
  trigger,
  imageUrl,
  id,
}: {
  trigger: React.ReactNode;
  imageUrl: string;
  id: string;
}) => {
  const [image, setImage] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  const handleSubmitForm = async (
    e: React.FormEvent<HTMLFormElement>
  ) => {
    e.preventDefault();

    if (loading) return;

    if (!image) {
      alert("Please upload an image.");
      return;
    }

    setLoading(true);

    try {
      const uploadedImage = await uploadImage(image);

      if (uploadedImage.status === 200) {
        const newImageUrl = uploadedImage.data.url;

        try {
          const res = await updateStory(id, {
            image: newImageUrl,
          });

          if (res?.status === 200) {
            setOpen(false);
          } else {
            toast.warning("Unknown error occurred. Try again.");
          }
        } catch (err) {
          const error = err as AxiosError;

          if (
            error.response &&
            (error.response.status === 400 ||
              error.response.status === 404 ||
              error.response.status === 401)
          ) {
            toast.error(
              "Error while updating story. " +
                (error.response.data as { message: string }).message
            );
          } else {
            toast.error(
              "Error while updating story. " + error.message
            );
          }
        }

        window.location.reload();
      } else {
        toast.warning("Unknown error occurred. Try again.");
      }
    } catch (err) {
      const error = err as AxiosError;

      toast.error(
        "Error while uploading the image. " + error.message
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <span className="p-1">{trigger}</span>
      </DialogTrigger>

      <DialogContent
        className="
          max-h-[90vh]
          overflow-y-auto
          sm:max-w-[620px]
          p-0
          rounded-sm
          bg-[#E8DFC8]
          text-[#241F1A]
          border-2
          border-[#C9A063]
          shadow-2xl
          overflow-visible
        "
      >
        <div
          className="
            relative
            p-6
            sm:p-8
          "
          style={{
            backgroundImage:
              "radial-gradient(rgba(107,74,50,0.12) 1px, transparent 1px)",
            backgroundSize: "14px 14px",
          }}
        >
          {/* Decorative pin */}
          <Pin
            className="
              absolute
              -top-3
              left-1/2
              -translate-x-1/2
              w-7
              h-7
              rotate-[-8deg]
              drop-shadow-md
              z-10
            "
            style={{ color: "#D9A441" }}
            fill="#D9A441"
          />

          <DialogHeader className="text-left mb-7">
            <span className="font-hand text-2xl text-[#B23A2E]">
              refresh a memory
            </span>

            <DialogTitle
              className="
                font-display
                text-3xl
                font-bold
                text-[#241F1A]
              "
            >
              Update Story
            </DialogTitle>

            <DialogDescription
              className="
                text-[#4A4136]
                leading-relaxed
                text-sm
                max-w-lg
              "
            >
              Replace the image of this story with a new one.
              Your updated story will be published again.
            </DialogDescription>
          </DialogHeader>

          {loading && <SubmitLoader />}

          <form
            className="space-y-6"
            onSubmit={handleSubmitForm}
          >
            <div className="space-y-3">
              <Label
                className="
                  font-semibold
                  text-[#241F1A]
                "
              >
                Choose a new image
              </Label>

              <Input
                type="file"
                accept="image/*"
                required
                className="
                  cursor-pointer
                  bg-[#F3EBD9]
                  border-[#C9A063]
                  text-[#241F1A]
                  file:text-[#241F1A]
                  file:font-medium
                  hover:border-[#B23A2E]
                  focus-visible:ring-[#B23A2E]
                  file:mr-3
                  file:border-0
                  file:bg-[#B23A2E]
                  file:text-[#F3EBD9]
                  file:px-3
                  file:py-1.5
                  file:rounded-sm
                "
                onChange={(e) => {
                  const file = e.target.files?.[0] || null;
                  setImage(file);
                }}
              />
            </div>

            {/* Image preview */}
            <div
              className="
                relative
                flex
                justify-center
                items-center
                min-h-64
                p-4
                bg-[#DDD0B0]
                border
                border-[#C9A063]
                rounded-sm
                shadow-inner
              "
            >
              <Pin
                className="
                  absolute
                  -top-3
                  right-5
                  w-6
                  h-6
                  rotate-[12deg]
                  drop-shadow
                "
                style={{ color: "#D9A441" }}
                fill="#D9A441"
              />

              {image ? (
                <img
                  src={URL.createObjectURL(image)}
                  alt="New story preview"
                  className="
                    max-h-72
                    max-w-full
                    object-contain
                    rounded-sm
                    shadow-lg
                  "
                />
              ) : imageUrl ? (
                <img
                  src={imageUrl}
                  alt="Current story"
                  className="
                    max-h-72
                    max-w-full
                    object-contain
                    rounded-sm
                    shadow-lg
                  "
                />
              ) : (
                <span className="font-hand text-lg text-[#8A7F6C]">
                  No image
                </span>
              )}
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <Button
                type="button"
                variant="outline"
                className="
                  cursor-pointer
                  rounded-sm
                  border-[#6B4A32]
                  bg-transparent
                  text-[#241F1A]
                  hover:bg-[#DDD0B0]
                "
                onClick={() => {
                  setImage(null);
                  setOpen(false);
                }}
              >
                Cancel
              </Button>

              <Button
                type="submit"
                disabled={loading}
                className="
                  cursor-pointer
                  rounded-sm
                  bg-[#B23A2E]
                  text-[#F3EBD9]
                  hover:bg-[#8F2E25]
                "
              >
                Update Story
              </Button>
            </div>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
};