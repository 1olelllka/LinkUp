import { updatePost } from "@/services/postServices";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { uploadImage } from "@/services/imageServices";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { usePostDetails } from "@/hooks/usePostDetails";
import { toast } from "sonner";
import type { AxiosError } from "axios";


export const UpdatePost = () => {
  const [image, setImage] = useState<File | null>(null);
  const [imageUrl, setImageUrl] = useState("");
  const [desc, setDesc] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const postId = useParams()?.postId;
  const post = usePostDetails(postId ? parseInt(postId) : 0);

  useEffect(() => {
    if (post) {
        setImageUrl(post.image);
        setDesc(post.desc);
    }
  }, [post]);

  const handleSubmitForm = async (e: React.FormEvent<HTMLFormElement>) => {
    if (loading) return;
    e.preventDefault();

    if (!image) {
      alert("Please upload an image.");
      return;
    }

    setLoading(true);
    const uploadedImage = await uploadImage(image);
        if (uploadedImage.status == 200) {
      const imageUrl = uploadedImage.data.url;
      try {
        const res = await updatePost(postId ? parseInt(postId) : undefined, {image: imageUrl, desc: desc});
        if (res?.status == 201) {
          toast.success("Successfully created post!");
          navigate("/profile");
        }
        } catch (err) {
          const error = err as AxiosError;
          toast.error("Unexpcted error occured. " + error.message);
      }
    } else {
      toast.error("Unexpected error while uploading the image")
    }
    setLoading(false);
  };

  return (
    <div className="mx-2">
      <h2 className="text-2xl font-bold my-4">Update post</h2>
      <form className="w-100 space-y-5" onSubmit={handleSubmitForm}>
        {loading && (
          <>
            <h1 className="text-xl font-semibold">🔄Loading...</h1>
          </>
        )}
        <div className="space-y-3">
          <Label>Upload the image</Label>
          <Input
            type="file"
            accept="image/*"
            required
            className="cursor-pointer"
            onChange={(e) => {
              const file = e.target.files?.[0] || null;
              setImage(file);
            }}
          />
          {image 
          ? (
            <img
              src={URL.createObjectURL(image)}
              alt="preview"
              className="w-32 h-32 object-cover rounded-md mt-2"
            />
          )
          : (imageUrl.length != 0
          ? <img
              src={imageUrl}
              alt="preview"
              className="w-32 h-32 object-cover rounded-md mt-2"
            />
          : <h1>No image</h1>)
        }
        </div>

        <div className="space-y-3">
          <Label>Add description (optional)</Label>
          <textarea
            value={desc}
            onChange={(e) => setDesc(e.target.value)}
            className="h-50 w-100 p-2 border-1 rounded-md align-text-top"
          />
        </div>
        <div className="space-x-2">
          <Button type="submit">Update</Button>
        </div>
      </form>
    </div>
  );
}