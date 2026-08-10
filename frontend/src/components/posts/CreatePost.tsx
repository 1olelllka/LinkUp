import { useRef, useState } from "react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { uploadImage } from "@/services/imageServices";
import { createNewPost } from "@/services/postServices";
import { useProfileStore } from "@/store/useProfileStore";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { SubmitLoader } from "../load/SubmitLoader";
import { ImagePlus, Pin, X } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogClose,
} from "../ui/dialog";
import type { Post } from "@/types/Post";

type CreatePostProps = {
  onPostCreated?: (res: Post) => void;
};

export const CreatePost = ({ onPostCreated }: CreatePostProps) => {
  const [image, setImage] = useState<File | null>(null);
  const [desc, setDesc] = useState("");
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleSubmitForm = async (e: React.FormEvent) => {
    e.preventDefault();

    if (loading) return;

    if (!image) {
      toast.error("Please upload an image.");
      return;
    }

    setLoading(true);

    try {
      const uploadedImage = await uploadImage(image);

      if (uploadedImage.status !== 200) {
        toast.error("Unexpected error while uploading the image.");
        return;
      }

      const imageUrl = uploadedImage.data.url;

      const res = await createNewPost(
        useProfileStore.getState().profile?.id,
        {
          image: imageUrl,
          desc,
        },
      );

      if (res?.status === 201) {
        toast.success("Successfully created post!");

        setImage(null);
        setDesc("");
        setOpen(false);

        if (fileInputRef.current) {
          fileInputRef.current.value = "";
        }

        onPostCreated?.(res?.data);
      }
    } catch (err) {
      const error = err as AxiosError;

      toast.error(
        "Unexpected error occurred. " +
          (error.message || "Please try again later."),
      );
    } finally {
      setLoading(false);
    }
  };

  const handleReset = () => {
    setImage(null);
    setDesc("");

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  return (
    <Dialog
      open={open}
      onOpenChange={(value) => {
        if (!loading) {
          setOpen(value);

          if (!value) {
            handleReset();
          }
        }
      }}
    >
      <DialogTrigger asChild>
        <Button
          variant="outline"
          size="icon"
          className="
            cursor-pointer
            rounded-sm
            border-[#6B4A32]
            text-[#241F1A]
            hover:bg-[#B23A2E]
            hover:text-[#F3EBD9]
            hover:border-[#B23A2E]
          "
        >
          <ImagePlus className="w-5 h-5" />
        </Button>
      </DialogTrigger>

      <DialogContent
        className="
          sm:max-w-[550px]
          bg-[#E8DFC8]
          text-[#241F1A]
          border-[#C9A063]
          rounded-sm
          shadow-2xl
        "
      >
        <div
          className="relative"
          style={{
            backgroundImage:
              "radial-gradient(rgba(107,74,50,0.12) 1px, transparent 1px)",
            backgroundSize: "14px 14px",
          }}
        >
          {/* Pin */}
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
              pin something new
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
              Create new post
            </DialogTitle>

            <DialogDescription className="text-[#4A4136]">
              Share something with your people.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmitForm} className="space-y-6">
            {/* Image upload */}
            <div className="space-y-2">
              <Label
                className="
                  font-display
                  text-sm
                  text-[#241F1A]
                "
              >
                Upload the image
              </Label>

              <div
                className="
                  relative
                  border-2
                  border-dashed
                  border-[#C9A063]
                  bg-[#F3EBD9]
                  rounded-sm
                  p-4
                  transition-colors
                  hover:border-[#B23A2E]
                "
              >
                <Input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  required
                  disabled={loading}
                  className="
                    cursor-pointer
                    border-0
                    bg-transparent
                    shadow-none
                    file:mr-4
                    file:rounded-sm
                    file:border-0
                    file:bg-[#B23A2E]
                    file:px-4
                    file:pt-[0.3rem]
                    file:text-sm
                    file:font-medium
                    file:text-[#F3EBD9]
                    hover:file:bg-[#9C3226]
                  "
                  onChange={(e) => {
                    const file = e.target.files?.[0] || null;
                    setImage(file);
                  }}
                />

                <p className="font-hand text-lg text-[#8A7F6C] mt-2">
                  📌 choose something worth pinning
                </p>
              </div>
            </div>

            {/* Image preview */}
            {image && (
              <div
                className="
                  relative
                  bg-[#F3EBD9]
                  border
                  border-[#C9A063]
                  p-3
                  shadow-md
                  rotate-[-1deg]
                "
              >
                <button
                  type="button"
                  disabled={loading}
                  onClick={() => {
                    setImage(null);

                    if (fileInputRef.current) {
                      fileInputRef.current.value = "";
                    }
                  }}
                  className="
                    absolute
                    -top-2
                    -right-2
                    z-10
                    flex
                    items-center
                    justify-center
                    w-7
                    h-7
                    rounded-full
                    bg-[#B23A2E]
                    text-[#F3EBD9]
                    shadow-md
                    hover:bg-[#9C3226]
                    disabled:opacity-50
                  "
                  aria-label="Remove image"
                >
                  <X className="w-4 h-4" />
                </button>

                <img
                  src={URL.createObjectURL(image)}
                  alt="Selected post"
                  className="
                    w-full
                    max-h-[320px]
                    object-cover
                    rounded-sm
                  "
                />
              </div>
            )}

            {/* Description */}
            <div className="space-y-2">
              <div className="flex items-center justify-between">
                <Label
                  className="
                    font-display
                    text-sm
                    text-[#241F1A]
                  "
                >
                  Description
                </Label>

                <span className="font-hand text-lg text-[#8A7F6C]">
                  optional
                </span>
              </div>

              <textarea
                value={desc}
                onChange={(e) => setDesc(e.target.value)}
                disabled={loading}
                maxLength={500}
                placeholder="What's on your mind?"
                className="
                  w-full
                  min-h-[130px]
                  resize-none
                  rounded-sm
                  border
                  border-[#C9A063]
                  bg-[#F3EBD9]
                  p-3
                  text-sm
                  text-[#241F1A]
                  placeholder:text-[#8A7F6C]
                  outline-none
                  focus:border-[#D9A441]
                  focus:ring-2
                  focus:ring-[#D9A441]/30
                  disabled:opacity-60
                "
              />

              <p className="text-right text-xs text-[#8A7F6C]">
                {desc.length}/500
              </p>
            </div>

            {/* Footer */}
            <DialogFooter
              className="
                pt-5
                border-t
                border-[#C9A063]
                sm:justify-between
                gap-3
              "
            >
              <Button
                type="button"
                variant="outline"
                disabled={loading}
                onClick={handleReset}
                className="
                  rounded-sm
                  border-[#8A7F6C]
                  bg-transparent
                  text-[#4A4136]
                  font-display
                  hover:bg-[#DDD0B0]
                  hover:text-[#241F1A]
                "
              >
                Reset
              </Button>

              <div className="flex gap-2">
                <DialogClose asChild>
                  <Button
                    type="button"
                    variant="outline"
                    disabled={loading}
                    className="
                      rounded-sm
                      border-[#8A7F6C]
                      bg-transparent
                      text-[#4A4136]
                      font-display
                      hover:bg-[#DDD0B0]
                      hover:text-[#241F1A]
                    "
                  >
                    Cancel
                  </Button>
                </DialogClose>

                <Button
                  type="submit"
                  disabled={loading || !image}
                  className="
                    rounded-sm
                    bg-[#B23A2E]
                    text-[#F3EBD9]
                    font-display
                    hover:bg-[#9C3226]
                    disabled:opacity-50
                  "
                >
                  {loading ? <SubmitLoader /> : "Pin it!"}
                </Button>
              </div>
            </DialogFooter>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
};