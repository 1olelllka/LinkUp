
import { useState } from "react";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "../ui/dialog";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import { useNavigate } from "react-router";
import { uploadImage } from "@/services/imageServices";
import { updateStory } from "@/services/storyServices";

export const UpdateStoryDialog = ({trigger, imageUrl, id} : {trigger: React.ReactNode, imageUrl: string, id: string}) => {
    const [image, setImage] = useState<File | null>(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);

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
            const res = await updateStory(id, {image: imageUrl});
            if (res?.status == 200) {
                setOpen(false);
                navigate("/archive");
            }
        }
        setLoading(false);
        window.location.reload();
    }

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <span className="p-1">
                    {trigger}
                </span>
            </DialogTrigger>
            <DialogContent>
                <DialogTitle>Update Story</DialogTitle>
                <form className="space-y-5" onSubmit={handleSubmitForm}>
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
                <div className="space-x-2">
                    <Button type="submit">Submit</Button>
                </div>
            </form>
            </DialogContent>
        </Dialog>
    );
}