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
import { useNavigate } from "react-router";
import { uploadImage } from "@/services/imageServices";
import { useProfileStore } from "@/store/useProfileStore";
import { createNewStory } from "@/services/storyServices";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { SubmitLoader } from "../load/SubmitLoader";
import { Pin } from "lucide-react";

export const CreateStoryDialog = ({
  trigger,
}: {
  trigger: React.ReactNode;
}) => {
  const [image, setImage] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
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

      if (uploadedImage.status == 200) {
        const imageUrl = uploadedImage.data.url;

        try {
          const res = await createNewStory(
            useProfileStore.getState().profile?.id,
            { image: imageUrl }
          );

          if (res?.status == 201) {
            setOpen(false);
            navigate("/archive");
            toast.success("Successfully created new story!");
          } else {
            toast.warning("Unknown error occured. Try again.");
          }
        } catch (err) {
          const error = err as AxiosError;

          if (
            error.response &&
            (error.response.status == 400 ||
              error.response.status == 404 ||
              error.response.status == 401)
          ) {
            toast.error(
              "Error while creating new story. " +
                (error.response.data as { message: string }).message
            );
          } else {
            toast.error(
              "Error while creating new story. " + error.message
            );
          }
        }
      } else {
        toast.warning("Unknown error occured. Try again.");
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
        {trigger}
      </DialogTrigger>

      <DialogContent
        className="
          max-h-[90vh]
          overflow-y-auto
          sm:max-w-[620px]
          p-3
          sm:p-5
          rounded-md
          bg-[#E8DFC8]
          text-[#241F1A]
          border-2
          border-[#C9A063]
          shadow-2xl
        "
      >
        {loading && <SubmitLoader />}

        <div
          className="relative p-2 sm:p-4"
          style={{
            backgroundImage:
              "radial-gradient(rgba(107,74,50,0.12) 1px, transparent 1px)",
            backgroundSize: "14px 14px",
          }}
        >
          <Pin
            className="
              absolute
              -top-4
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
              a new memory
            </span>

            <DialogTitle
              className="
                font-display
                text-2xl
                sm:text-3xl
                font-bold
                text-[#241F1A]
              "
            >
              Create Story
            </DialogTitle>

            <DialogDescription
              className="
                text-[#4A4136]
                leading-relaxed
                text-sm
              "
            >
              Share a moment with your subscribers. Your story
              will be visible for 24 hours.
            </DialogDescription>
          </DialogHeader>

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
                Upload the image
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
                  file:bg-[#DDD0B0]
                  file:text-[#241F1A]
                  file:border-0
                  file:mr-3
                  file:px-3
                  file:py-1
                  file:rounded-sm
                  hover:border-[#B23A2E]
                "
                onChange={(e) => {
                  const file = e.target.files?.[0] || null;
                  setImage(file);
                }}
              />

              {image && (
                <div className="flex justify-center pt-2">
                  <div
                    className="
                      relative
                      p-2
                      bg-[#F3EBD9]
                      border
                      border-[#C9A063]
                      shadow-md
                      rotate-[-1deg]
                    "
                  >
                    <img
                      src={URL.createObjectURL(image)}
                      alt="preview"
                      className="
                        w-40
                        h-40
                        sm:w-48
                        sm:h-48
                        object-cover
                      "
                    />
                  </div>
                </div>
              )}
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="reset"
                variant="outline"
                className="
                  cursor-pointer
                  border-[#6B4A32]
                  text-[#241F1A]
                  bg-transparent
                  hover:bg-[#DDD0B0]
                  hover:text-[#241F1A]
                  rounded-sm
                "
                onClick={() => setImage(null)}
              >
                Reset
              </Button>

              <Button
                type="submit"
                className="
                  cursor-pointer
                  bg-[#B23A2E]
                  text-[#F3EBD9]
                  hover:bg-[#8F2E25]
                  rounded-sm
                  shadow-sm
                "
              >
                Publish Story
              </Button>
            </div>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
};