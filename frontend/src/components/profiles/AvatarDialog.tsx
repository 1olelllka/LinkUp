import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
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

export const AvatarDialog = ({children, setProfile} : {children: React.ReactNode, setProfile: React.Dispatch<React.SetStateAction<Profile>>}) => {

    const [loading, setLoading] = useState(false);
    const [image, setImage] = useState<File | null>(null);
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
                    const res = await patchPersonalProfileInfo(useProfileStore.getState().profile?.userId, {photo: imageUrl});
                    if (res) {
                        setOpen(false);
                        setProfile((prev) => 
                        ({
                            ...prev,
                            photo: imageUrl
                        }));
                        toast.success("Successfully updated profile!");
                    } else {
                        toast.warning("Unknown error occured. Try again.")
                    }
                } catch (err) {
                    const error = err as AxiosError;
                    if (error.response && (error.response.status == 400 || error.response.status == 404 || error.response.status == 401)) {
                        toast.error("Error while updating profile information. " + (error.response.data as {message: string}).message);
                    } else {
                        toast.error("Error while updating profile information. " + error.message);
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
        <DialogTrigger onClick={() => console.log('sdfa')}>
            {children}
        </DialogTrigger>
        <DialogContent>
            {loading && (
                <SubmitLoader />
            )}
            <DialogHeader>
                <DialogTitle>Change your profile picture</DialogTitle>
            </DialogHeader>
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