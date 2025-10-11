import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import {
  createNewCommentForSpecificPost,
  deleteSpecificComment,
  getAllCommentsForSpecificPost,
} from "@/services/postServices";
import type { Comment, CommentPage } from "@/types/Post";
import type { AxiosError } from "axios";
import type React from "react";
import { useCallback, useEffect, useState } from "react";
import { toast } from "sonner";
import { Comments } from "../posts/Comments";
import { CommentForm } from "../posts/CommentForm";

export const FeedCommentSheet = ({
  postId,
  children,
}: {
  postId: number;
  children: React.ReactNode;
}) => {
  const [open, setOpen] = useState(false);
  const [shouldFetch, setShouldFetch] = useState(false);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentPage, setCommentPage] = useState<CommentPage>();
  const [loading, setLoading] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);

  useEffect(() => {
    if (open) {
      setShouldFetch(true);
    }
  }, [open]);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const commentData = await getAllCommentsForSpecificPost(postId, 1);
        setCommentPage(commentData);
        setComments(commentData.results);
      } catch (err) {
        const error = err as AxiosError;
        if (error.status != 404) {
          toast.error("Failed to fetch comments. " + error.message);
        }
      }
    };

    if (postId && shouldFetch) {
      fetchData();
    }
  }, [shouldFetch, postId]);

  const loadMoreComments = useCallback(async () => {
    if (loading || !postId) return;
    setLoading(true);
    try {
      const res = await getAllCommentsForSpecificPost(postId, currentPage + 1);
      setCurrentPage((prevPage) => prevPage + 1);
      setCommentPage(res);
      setComments((prev) => [...prev, ...res.results]);
    } catch (err) {
      const error = err as AxiosError;
      toast.error("Failed to load more comments. " + error.message);
    } finally {
      setLoading(false);
    }
  }, [loading, postId, currentPage]);

  const handleAddReply = async (
    postId: number,
    parentId: number,
    text: string
  ) => {
    try {
      const newReply = await createNewCommentForSpecificPost({
        post: postId,
        parent: parentId,
        text: text,
      });
      setComments((prev = []) =>
        prev.map((comment) =>
          comment.id === parentId
            ? {
                ...comment,
                replies: [...(comment.replies || []), newReply],
              }
            : comment
        )
      );
    } catch (err) {
      const error = err as AxiosError;
      toast.error("Failed to post reply. " + error.message);
    }
  };

  const handleAddTopLevelComment = async (postId: number, text: string) => {
    try {
      const newComment = await createNewCommentForSpecificPost({
        post: postId,
        text: text,
      });

      setComments((prev = []) => [...prev, newComment]);
    } catch (err) {
      const error = err as AxiosError;
      toast.error("Failed to post new comment. " + error.message);
    }
  };

  const handleDeleteComment = async (id: number) => {
    try {
      const status = await deleteSpecificComment(id);
      if (status === 204) {
        setComments((prev = []) => {
          const removeCommentRecursively = (comments: Comment[]): Comment[] => {
            return comments
              .filter((c) => c.id !== id)
              .map((c) => ({
                ...c,
                replies: c.replies
                  ? removeCommentRecursively(c.replies)
                  : undefined,
              }));
          };

          return removeCommentRecursively(prev);
        });
      }
    } catch (err) {
      const error = err as AxiosError;
      toast.error("Failed to delete the comment. " + error.message);
    }
  };

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger>{children}</SheetTrigger>
      <SheetContent className="overflow-y-auto">
        <SheetHeader className="pb-0">
          <SheetTitle className="text-3xl">Comments</SheetTitle>
        </SheetHeader>
        <div className="px-4">
          <CommentForm
            postId={postId || 0}
            onSubmit={handleAddTopLevelComment}
          />
        </div>
        <div className="mt-4 flex-1 min-h-0 px-4 pb-2">
          {comments?.length === 0 ? (
            <p className="text-sm text-muted-foreground">No comments yet.</p>
          ) : (
            comments?.map((comment) => (
              <Comments
                postId={postId || 0}
                key={comment.id}
                comment={comment}
                parentCommentId={comment.id}
                addReply={handleAddReply}
                deleteComment={handleDeleteComment}
              />
            ))
          )}
          {commentPage && commentPage.next != null && (
            <div className="mt-2">
              {loading ? (
                <p className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400">
                  🔄 Loading...
                </p>
              ) : (
                <p
                  className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400"
                  onClick={loadMoreComments}
                >
                  🚀 Load More
                </p>
              )}
            </div>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
};
