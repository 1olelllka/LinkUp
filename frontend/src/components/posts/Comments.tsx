import { useState } from "react";
import { CustomAvatar } from "../profiles/CustomAvatar";
import type { Comment } from "@/types/Post";
import { CommentForm } from "./CommentForm";
import { useProfileStore } from "@/store/useProfileStore";
import { Trash } from "lucide-react";
import type { AxiosError } from "axios";
import { toast } from "sonner";
import { Link } from "react-router";

export function Comments({
  postId,
  comment,
  addReply,
  parentCommentId,
  deleteComment,
}: {
  postId: number;
  comment: Comment;
  parentCommentId: number;
  addReply: (postId: number, parentId: number, text: string) => void;
  deleteComment?: (commentId: number) => void;
  currentUserId?: number;
}) {
  const [showReplies, setShowReplies] = useState(false);
  const [replying, setReplying] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const currentUserId = useProfileStore.getState().profile?.id;

  const canDelete = currentUserId && (currentUserId === comment.user_id);

  const handleDelete = async () => {
    if (deleteComment) {
      try {
        deleteComment(comment.id);
        toast.success("Successfully deleted comment!");
      } catch (err) {
        const error = err as AxiosError;
        toast.error("Failed to delete comment. " + error.message);
      }
    }
    setShowDeleteConfirm(false);
  };

  return (
    <div className="border border-[#C9A063] bg-[#F3EBD9] p-3 rounded-sm mb-3">
      {/* Main comment */}
      <div className="flex items-center justify-between mb-1">
        <div className="flex items-center gap-2">
          <CustomAvatar name={comment?.name} photo={comment?.photo} size={32} />
          <div>
            <p className="font-medium text-sm text-[#241F1A]">{comment?.name}</p>
            <Link to={"/profile/" + comment.user_id}>
              <p className="text-xs text-[#8A7F6C] hover:text-[#B23A2E] hover:underline">@{comment?.username}</p>
            </Link>
          </div>
        </div>
        
        {canDelete && (
          <div className="relative">
            <button
              onClick={() => setShowDeleteConfirm(!showDeleteConfirm)}
              className="text-[#8A7F6C] hover:text-[#B23A2E] p-1 rounded-sm"
              title="Delete comment"
            >
              <Trash size={16}/>
            </button>
            
            {showDeleteConfirm && (
              <div className="absolute right-0 top-8 bg-[#F3EBD9] border border-[#C9A063] rounded-sm shadow-lg p-3 z-10 min-w-48">
                <p className="text-sm text-[#4A4136] mb-3">Delete this comment?</p>
                <div className="flex gap-2">
                  <button
                    onClick={handleDelete}
                    className="px-3 py-1 bg-[#B23A2E] text-[#F3EBD9] text-sm rounded-sm hover:bg-[#9c3226]"
                  >
                    Delete
                  </button>
                  <button
                    onClick={() => setShowDeleteConfirm(false)}
                    className="px-3 py-1 bg-[#DDD0B0] text-[#241F1A] text-sm rounded-sm hover:bg-[#C9A063]"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
      
      <p className="text-sm text-[#241F1A]">{comment.text}</p>

      {/* Reply button */}
      <div className="mt-2 space-x-4">
        {comment.replies && comment.replies?.length > 0 && (
          <button
            onClick={() => setShowReplies(!showReplies)}
            className="text-sm text-[#B23A2E] hover:underline"
          >
            {showReplies ? "Hide replies" : `View replies (${comment.replies.length})`}
          </button>
        )}
        <button
          onClick={() => setReplying((prev) => !prev)}
          className="text-sm text-[#4A4136] hover:text-[#241F1A]"
        >
          {replying ? "Cancel" : "Reply"}
        </button>
      </div>

      {replying && (
        <div className="mt-2">
          <CommentForm
            postId={postId}
            onSubmit={(postId, text) => {
              addReply(postId, parentCommentId, text);
              setReplying(false);
              setShowReplies(true);
            }}
            profileReply={comment.username}
            autoFocus
            placeholder="Write a reply..."
          />
        </div>
      )}

      {/* Nested replies */}
      {showReplies && comment.replies && (
        <div className="mt-3 pl-4 border-l border-[#C9A063] space-y-3">
          {comment.replies.map((reply) => (
            <Comments
              key={reply.id}
              parentCommentId={comment.id}
              comment={reply}
              postId={postId}
              addReply={addReply}
              deleteComment={deleteComment}
            />
          ))}
        </div>
      )}
    </div>
  );
}