import { updatePost } from "@/services/postServices";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { uploadImage } from "@/services/imageServices";
import { useEffect, useState } from "react";
import { usePostDetails } from "@/hooks/usePostDetails";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { SubmitLoader } from "../load/SubmitLoader";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "../ui/dialog";
import { Pin } from "lucide-react";

type UpdatePostProps = {
  postId: number | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onPostUpdated?: () => void;
};

export const UpdatePost = ({
  postId,
  open,
  onOpenChange,
  onPostUpdated,
}: UpdatePostProps) => {
  const [image, setImage] = useState<File | null>(null);
  const [imageUrl, setImageUrl] = useState("");
  const [desc, setDesc] = useState("");
  const [loading, setLoading] = useState(false);

  const { post, setPost } = usePostDetails(
    open && postId ? postId : 0
  );

  useEffect(() => {
    if (post && open) {
      setImageUrl(post.image || "");
      setDesc(post.desc || "");
      setImage(null);
    }
  }, [post, open]);

  const handleSubmitForm = async (
    e: React.FormEvent<HTMLFormElement>
  ) => {
    e.preventDefault();

    if (loading || !postId) return;

    setLoading(true);

    try {
      let newImageUrl = imageUrl;

      if (image) {
        const uploadedImage = await uploadImage(image);

        if (uploadedImage.status !== 200) {
          toast.error(
            "Unexpected response while uploading the image."
          );
          return;
        }

        newImageUrl = uploadedImage.data.url;
      }

      const res = await updatePost(postId, {
        image: newImageUrl,
        desc,
      });

      if (res?.status === 200) {
        setPost((prev) => ({
          ...prev,
          image: res.data.image,
          desc: res.data.desc,
        }));

        toast.success("Successfully updated post!");

        onOpenChange(false);
        onPostUpdated?.();
      }
    } catch (err) {
      const error = err as AxiosError;

      toast.error(
        "Unexpected error occurred. " +
          (error.message || "Please try again.")
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog
      open={open}
      onOpenChange={(value) => {
        if (!loading) {
          onOpenChange(value);
        }
      }}
    >
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
        <div className="relative"
          style={{
            backgroundImage:
              "radial-gradient(rgba(107,74,50,0.12) 1px, transparent 1px)",
            backgroundSize: "14px 14px",
          }}>
          <Pin
            className="
              absolute
              -top-7
              left-1/2
              -translate-x-1/2
              w-6
              h-6
              z-20
              rotate-[-10deg]
              drop-shadow
            "
            style={{ color: "#D9A441" }}
            fill="#D9A441"
          />

          <DialogHeader>
            <span className="font-hand text-xl text-[#D9A441]">
              change your note
            </span>

            <DialogTitle className="font-display text-2xl font-bold text-[#241F1A]">
              Update post
            </DialogTitle>

            <DialogDescription className="text-[#4A4136]">
              Make changes to your post here. Click update when
              you're done.
            </DialogDescription>
          </DialogHeader>

          {loading && <SubmitLoader />}

          <form
            onSubmit={handleSubmitForm}
            className="space-y-5 mt-5"
          >
            <div className="space-y-3">
              <Label className="font-display font-bold">
                Upload the image
              </Label>

              <Input
                type="file"
                accept="image/*"
                className="
                  cursor-pointer
                  bg-[#F3EBD9]
                  border-[#C9A063]
                  rounded-sm
                  text-[#241F1A]
                  file:text-[#241F1A]
                "
                onChange={(e) => {
                  const file = e.target.files?.[0] || null;
                  setImage(file);
                }}
              />

              {image ? (
                <img
                  src={URL.createObjectURL(image)}
                  alt="New post preview"
                  className="
                    w-full
                    h-52
                    object-cover
                    rounded-sm
                    border
                    border-[#C9A063]
                    shadow-md
                  "
                />
              ) : imageUrl ? (
                <img
                  src={imageUrl}
                  alt="Current post"
                  className="
                    w-full
                    h-52
                    object-cover
                    rounded-sm
                    border
                    border-[#C9A063]
                    shadow-md
                  "
                />
              ) : (
                <div
                  className="
                    w-full
                    h-52
                    flex
                    items-center
                    justify-center
                    bg-[#DDD0B0]
                    border
                    border-[#C9A063]
                    rounded-sm
                    text-[#8A7F6C]
                    text-sm
                  "
                >
                  No image
                </div>
              )}
            </div>

            <div className="space-y-3">
              <Label className="font-display font-bold">
                Add description{" "}
                <span className="font-normal text-[#8A7F6C]">
                  (optional)
                </span>
              </Label>

              <textarea
                value={desc}
                onChange={(e) => setDesc(e.target.value)}
                maxLength={300}
                placeholder="Write something about this moment..."
                className="
                  w-full
                  min-h-32
                  p-3
                  resize-y
                  bg-[#F3EBD9]
                  border
                  border-[#C9A063]
                  rounded-sm
                  text-[#241F1A]
                  placeholder:text-[#8A7F6C]
                  outline-none
                  focus:ring-2
                  focus:ring-[#D9A441]
                  focus:border-[#D9A441]
                "
              />
            </div>

            <DialogFooter className="sm:justify-end gap-2">
              <Button
                type="button"
                variant="outline"
                disabled={loading}
                onClick={() => onOpenChange(false)}
                className="
                  rounded-sm
                  border-[#6B4A32]
                  text-[#241F1A]
                  hover:bg-[#DDD0B0]
                  cursor-pointer
                "
              >
                Cancel
              </Button>

              <Button
                type="submit"
                disabled={loading}
                className="
                  rounded-sm
                  bg-[#B23A2E]
                  hover:bg-[#9C3226]
                  text-[#F3EBD9]
                  cursor-pointer
                "
              >
                Update post
              </Button>
            </DialogFooter>
          </form>
        </div>
      </DialogContent>
    </Dialog>
  );
};