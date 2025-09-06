
import { useState } from "react";
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from "../ui/dialog";
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

export const CreateStoryDialog = ({trigger} : {trigger: React.ReactNode}) => {
    const [image, setImage] = useState<File | null>(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const [open, setOpen] = useState(false);

    const handleSubmitForm = async (e: React.FormEvent<HTMLFormElement>) => {
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
                    const res = await createNewStory(useProfileStore.getState().profile?.userId, {image: imageUrl});
                    if (res?.status == 201) {
                        setOpen(false);
                        navigate("/archive");
                        toast.success("Successfully created new story!");
                    } else {
                        toast.warning("Unknown error occured. Try again.")
                    }
                } catch (err) {
                    const error = err as AxiosError;
                    if (error.response && (error.response.status == 400 || error.response.status == 404 || error.response.status == 401)) {
                        toast.error("Error while creating new story. " + (error.response.data as {message: string}).message);
                    } else {
                        toast.error("Error while creating new story. " + error.message);
                    }
                }
            } else {
                toast.warning("Unknown error occured. Try again.");
            }
        } catch (err) {
            const error = err as AxiosError;
            toast.error("Error while uploading the image. " + error.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger>
                {trigger}
            </DialogTrigger>
            <DialogContent>
                {loading && (
                <SubmitLoader />
                )}
                <DialogTitle>Create Story</DialogTitle>
                <form className="space-y-5" onSubmit={handleSubmitForm}>
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
                <div className="space-x-2">
                    <Button type="reset" variant={"outline"}>Reset</Button>
                    <Button type="submit">Submit</Button>
                </div>
            </form>
            </DialogContent>
        </Dialog>
    );
}