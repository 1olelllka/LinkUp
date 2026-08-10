import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { uploadImage } from "@/services/imageServices";
import type { AxiosError } from "axios";
import type React from "react";
import { toast } from "sonner";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { useState } from "react";
import { SubmitLoader } from "../load/SubmitLoader";
import { patchPersonalProfileInfo } from "@/services/profileServices";
import { useProfileStore } from "@/store/useProfileStore";
import type { Profile } from "@/types/Profile";

export const AvatarDialog = ({
  children,
  setProfile,
}: {
  children: React.ReactNode;
  setProfile: React.Dispatch<React.SetStateAction<Profile>>;
}) => {
  const [loading, setLoading] = useState(false);
  const [image, setImage] = useState<File | null>(null);
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
          const res = await patchPersonalProfileInfo(
            useProfileStore.getState().profile?.id,
            { photo: imageUrl }
          );

          if (res) {
            setOpen(false);
            setProfile((prev) => ({
              ...prev,
              photo: imageUrl,
            }));
            toast.success("Successfully updated profile!");
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
              "Error while updating profile information. " +
                (error.response.data as { message: string }).message
            );
          } else {
            toast.error(
              "Error while updating profile information. " + error.message
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
      <DialogTrigger>{children}</DialogTrigger>

      <DialogContent
        className="
          sm:max-w-[480px]
          bg-[#E8DFC8]
          text-[#241F1A]
          border
          border-[#C9A063]
          rounded-sm
          shadow-2xl
          p-0
          overflow-visible
        "
      >
        {loading && <SubmitLoader />}

        <div className="relative p-6 pt-8">
          {/* Pin */}
          <div
            className="
              absolute
              -top-3
              left-1/2
              -translate-x-1/2
              w-5
              h-5
              rounded-full
              bg-[#D9A441]
              border-2
              border-[#6B4A32]
              shadow-md
              rotate-[-8deg]
            "
          />

          <DialogHeader className="mb-6">
            <span className="font-hand text-xl text-[#D9A441]">
              new picture
            </span>

            <DialogTitle className="font-display text-2xl font-bold text-[#241F1A]">
              Change your profile picture
            </DialogTitle>
          </DialogHeader>

          <form
            className="space-y-6"
            onSubmit={handleSubmitForm}
          >
            <div className="space-y-3">
              <Label className="font-display text-sm font-bold text-[#241F1A]">
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
                  rounded-sm
                  text-[#241F1A]
                  file:mr-3
                  file:border-0
                  file:bg-[#B23A2E]
                  file:text-[#F3EBD9]
                  file:px-3
                  file:py-1.5
                  file:rounded-sm
                  file:cursor-pointer
                  hover:border-[#B23A2E]
                  focus-visible:ring-[#D9A441]
                "
                onChange={(e) => {
                  const file = e.target.files?.[0] || null;
                  setImage(file);
                }}
              />

              {image && (
                <div
                  className="
                    relative
                    inline-block
                    bg-[#F3EBD9]
                    border
                    border-[#C9A063]
                    rounded-sm
                    p-2
                    shadow-md
                    rotate-[-1deg]
                    mt-2
                  "
                >
                  <div
                    className="
                      absolute
                      -top-2
                      left-1/2
                      -translate-x-1/2
                      w-4
                      h-4
                      rounded-full
                      bg-[#D9A441]
                      border
                      border-[#6B4A32]
                      shadow-sm
                    "
                  />

                  <img
                    src={URL.createObjectURL(image)}
                    alt="preview"
                    className="
                      w-32
                      h-32
                      object-cover
                      rounded-sm
                    "
                  />
                </div>
              )}
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <Button
                type="reset"
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
                Reset
              </Button>

              <Button
                type="submit"
                className="
                  rounded-sm
                  bg-[#B23A2E]
                  text-[#F3EBD9]
                  font-medium
                  hover:bg-[#9c3226]
                "
              >
                Submit
              </Button>
            </div>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
};