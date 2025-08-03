import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { z } from "zod";

const commentSchema = z.object({
  text: z.string().min(1, "Comment cannot be empty"),
});

export function CommentForm({postId, onSubmit, autoFocus = false, placeholder = "Add a comment...",}: {
  postId: number,
  onSubmit: (postId: number, text: string) => void;
  autoFocus?: boolean;
  placeholder?: string;
}) {
  const {register, handleSubmit, reset, formState: { errors }} = useForm<z.infer<typeof commentSchema>>({
    resolver: zodResolver(commentSchema),
  });

  return (
    <form
      onSubmit={handleSubmit((data) => {
        onSubmit(postId, data.text);
        reset();
      })}
      className="flex flex-col gap-2 mt-2"
    >
      <Input
        {...register("text")}
        placeholder={placeholder}
        autoFocus={autoFocus}
      />
      {errors.text && (
        <span className="text-xs text-red-500">{errors.text.message}</span>
      )}
      <Button type="submit" size="sm" className="self-end">
        Post
      </Button>
    </form>
  );
}
