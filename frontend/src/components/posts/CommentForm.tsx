import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { z } from "zod";

const commentSchema = z.object({
  text: z.string().min(1, "Comment cannot be empty"),
});

export function CommentForm({postId, onSubmit, autoFocus = false, placeholder = "Add a comment...", profileReply}: {
  postId: number,
  onSubmit: (postId: number, text: string) => void;
  autoFocus?: boolean;
  placeholder?: string;
  profileReply?: string;
}) {
  const {register, handleSubmit, reset, formState: { errors }} = useForm<z.infer<typeof commentSchema>>({
    resolver: zodResolver(commentSchema),
    defaultValues: {
      text: profileReply ? "@" + profileReply + " " : ""
    }
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
        className="bg-[#F3EBD9] border-[#C9A063] text-[#241F1A] placeholder:text-[#8A7F6C] rounded-sm focus-visible:ring-[#D9A441] focus-visible:border-[#B23A2E]"
      />
      {errors.text && (
        <span className="text-xs text-[#B23A2E]">{errors.text.message}</span>
      )}
      <Button type="submit" size="sm" className="self-end bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9] rounded-sm">
        Post
      </Button>
    </form>
  );
}