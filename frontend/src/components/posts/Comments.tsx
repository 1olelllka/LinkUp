import { useState } from "react";
import { CustomAvatar } from "../profiles/CustomAvatar";
import type { Comment } from "@/types/Post";
import { CommentForm } from "./CommentForm";

export function Comments({postId, comment, addReply}: {postId: number, comment: Comment; addReply: (postId: number, parentId: number, text: string) => void}) {
  const [showReplies, setShowReplies] = useState(false);
  const [replying, setReplying] = useState(false);

  return (
    <div className="border p-3 rounded-lg mb-3">
      {/* Main comment */}
      <div className="flex items-center gap-2 mb-1">
        <CustomAvatar name={comment?.name} photo={comment?.photo} size={32} />
        <div>
          <p className="font-medium text-sm">{comment?.name}</p>
          <p className="text-xs text-muted-foreground">@{comment?.username}</p>
        </div>
      </div>
      <p className="text-sm text-gray-800">{comment.text}</p>

      {/* Reply button */}
      <div className="mt-2 space-x-4">
        {comment.replies?.length > 0 && (
          <button
            onClick={() => setShowReplies(!showReplies)}
            className="text-sm text-blue-600"
          >
            {showReplies ? "Hide replies" : `View replies (${comment.replies.length})`}
          </button>
        )}
        <button
          onClick={() => setReplying((prev) => !prev)}
          className="text-sm text-gray-600"
        >
          {replying ? "Cancel" : "Reply"}
        </button>
      </div>

      {/* Reply form */}
      {replying && (
        <div className="mt-2">
          <CommentForm
            postId={postId}
            onSubmit={(postId, text) => {
              addReply(postId, comment.id, text);
              setReplying(false);
              setShowReplies(true);
            }}
            autoFocus
            placeholder="Write a reply..."
          />
        </div>
      )}

      {/* Nested replies */}
      {showReplies && comment.replies && (
        <div className="mt-3 pl-4 border-l space-y-3">
          {comment.replies.map((reply) => (
            <Comments
              key={reply.id}
              comment={reply}
              postId={postId}
              addReply={addReply}
            />
          ))}
        </div>
      )}
    </div>
  );
}
