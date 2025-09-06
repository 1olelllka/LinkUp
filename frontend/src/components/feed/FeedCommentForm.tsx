import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "../ui/input"
import { Button } from "../ui/button"
import { z } from "zod"
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { createNewCommentForSpecificPost } from "@/services/postServices";
import type { AxiosError } from "axios";
import { toast } from "sonner";

const commentSchema = z.object({
  text: z.string().min(1, "Comment cannot be empty"),
});

export const FeedCommentForm = ({postId: postId} : {postId: number}) => {

    const {register, handleSubmit, reset, formState: { errors }} = useForm<z.infer<typeof commentSchema>>({
        resolver: zodResolver(commentSchema),
    });

    const onSubmit = async (postId: number, text : string) => {
    try {
        await createNewCommentForSpecificPost({post: postId, text: text});
        toast.success("Added new comment to the post!");
    } catch (err) {
        const error = err as AxiosError;
        toast.error(error.message);
    }
    }

    return (
        <div className="flex space-x-4 text-sm text-gray-500 pt-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <span className="cursor-pointer">💬 Comment</span>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <form
                onSubmit={handleSubmit((data) => {
                    onSubmit(postId, data.text);
                    reset();
                })}
                className="flex flex-col gap-2 mt-2"
                >
                <Input
                    {...register("text")}
                    placeholder={"Comment here..."}
                    autoFocus={true}
                />
                {errors.text && (
                    <span className="text-xs text-red-500">{errors.text.message}</span>
                )}
                <Button type="submit" size="sm" className="self-end">
                    Post
                </Button>
            </form>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    )
}