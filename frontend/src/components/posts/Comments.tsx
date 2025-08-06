import { useState } from "react";
import { CustomAvatar } from "../profiles/CustomAvatar";
import type { Comment } from "@/types/Post";
import { CommentForm } from "./CommentForm";
import { useProfileStore } from "@/store/useProfileStore";

export function Comments({
  postId,
  comment,
  addReply,
  deleteComment,
}: {
  postId: number;
  comment: Comment;
  addReply: (postId: number, parentId: number, text: string) => void;
  deleteComment?: (commentId: number) => void;
  currentUserId?: number;
}) {
  const [showReplies, setShowReplies] = useState(false);
  const [replying, setReplying] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const currentUserId = useProfileStore.getState().profile?.userId;

  // Check if current user can delete this comment
  const canDelete = currentUserId && (currentUserId === comment.user_id);

  const handleDelete = () => {
    if (deleteComment) {
      deleteComment(comment.id);
    }
    setShowDeleteConfirm(false);
  };

  return (
    <div className="border p-3 rounded-lg mb-3">
      {/* Main comment */}
      <div className="flex items-center justify-between mb-1">
        <div className="flex items-center gap-2">
          <CustomAvatar name={comment?.name} photo={comment?.photo} size={32} />
          <div>
            <p className="font-medium text-sm">{comment?.name}</p>
            <p className="text-xs text-muted-foreground">@{comment?.username}</p>
          </div>
        </div>
        
        {/* Delete button - only show if user can delete */}
        {canDelete && (
          <div className="relative">
            <button
              onClick={() => setShowDeleteConfirm(!showDeleteConfirm)}
              className="text-gray-400 hover:text-red-600 p-1 rounded"
              title="Delete comment"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M3 6h18l-1.5 14H4.5L3 6zm5-2V2h8v2h5v2H3V4h5zm2 0h4V2h-4v2z"/>
              </svg>
            </button>
            
            {/* Delete confirmation dropdown */}
            {showDeleteConfirm && (
              <div className="absolute right-0 top-8 bg-white border border-gray-200 rounded-lg shadow-lg p-3 z-10 min-w-48">
                <p className="text-sm text-gray-700 mb-3">Delete this comment?</p>
                <div className="flex gap-2">
                  <button
                    onClick={handleDelete}
                    className="px-3 py-1 bg-red-600 text-white text-sm rounded hover:bg-red-700"
                  >
                    Delete
                  </button>
                  <button
                    onClick={() => setShowDeleteConfirm(false)}
                    className="px-3 py-1 bg-gray-200 text-gray-700 text-sm rounded hover:bg-gray-300"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
      
      <p className="text-sm text-gray-800">{comment.text}</p>

      {/* Reply button */}
      <div className="mt-2 space-x-4">
        {comment.replies && comment.replies?.length > 0 && (
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
              deleteComment={deleteComment}
            />
          ))}
        </div>
      )}
    </div>
  );
}