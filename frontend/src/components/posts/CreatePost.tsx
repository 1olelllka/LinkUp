import { useState } from "react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { uploadImage } from "@/services/imageServices";
import { createNewPost } from "@/services/postServices";
import { useProfileStore } from "@/store/useProfileStore";
import { useNavigate } from "react-router";

export const CreatePost = () => {
  const [image, setImage] = useState<File | null>(null);
  const [desc, setDesc] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

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
      const res = await createNewPost(useProfileStore.getState().profile?.userId, {image: imageUrl, desc: desc});
      if (res?.status == 201) {
        navigate("/profile");
      }
    }
    setLoading(false);
  };

  return (
    <div className="mx-2">
      <h2 className="text-2xl font-bold my-4">Create new post</h2>
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
          {image && (
            <img
              src={URL.createObjectURL(image)}
              alt="preview"
              className="w-32 h-32 object-cover rounded-md mt-2"
            />
          )}
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
          <Button type="reset" variant={"outline"}>Reset</Button>
          <Button type="submit">Submit</Button>
        </div>
      </form>
    </div>
  );
};
