import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog";
import { CustomAvatar } from "../profiles/CustomAvatar";
import { useCallback, useEffect, useState } from "react";
import type { Comment, CommentPage, Post } from "@/types/Post";
import type { Profile } from "@/types/Profile";
import { createNewCommentForSpecificPost, deleteSpecificComment, getAllCommentsForSpecificPost, getPostDetailsById } from "@/services/postServices";
import { getSpecificProfileInfo } from "@/services/profileServices";
import { Comments } from "./Comments";
import { CommentForm } from "./CommentForm";
import type { AxiosError } from "axios";
import { toast } from "sonner";

export function PostModal({ postId, trigger }: { postId: number, trigger: React.ReactNode }) {
  const [open, setOpen] = useState(false);
  const [shouldFetch, setShouldFetch] = useState(false);
  const [post, setPost] = useState<Post>();
  const [profile, setProfile] = useState<Profile>();
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
        const postData = await getPostDetailsById(postId);
        setPost(postData);

        const profileData = await getSpecificProfileInfo(postData?.user_id);
        setProfile(profileData);

        const commentData = await getAllCommentsForSpecificPost(postId, 1);
        setCommentPage(commentData);
        setComments(commentData.results.sort((a : Comment, b : Comment) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime()));
      } catch (err) {
        const error = err as AxiosError;
        toast.error("Failed to fetch post/profile. " + error.message);
      }
    };

    if (shouldFetch && !post) {
      fetchData();
    }
  }, [shouldFetch, postId, post]);

    const loadMoreComments = useCallback(async () => {
      if (loading || !postId) return;
      setLoading(true);
      try {
        const res = await getAllCommentsForSpecificPost(postId, currentPage + 1);
        setCurrentPage(prevPage => prevPage + 1);
        setCommentPage(res);
        setComments(prev => [...prev, ...res.results]);
      } catch (err) {
        const error = err as AxiosError;
        toast.error("Failed to load more comments. " + error.message);
      } finally {
        setLoading(false);
      }
    }, [loading, postId, currentPage]);

  const handleAddReply = async (postId: number, parentId: number, text: string) => {
    try {
      const newReply = await createNewCommentForSpecificPost({post: postId, parent: parentId, text: text});

      setComments((prev = []) =>
        prev.map((comment) =>
          comment.id === parentId
            ? {
                ...comment,
                replies: [...(comment.replies || []), newReply],
              }
            : {
                ...comment,
                replies: comment.replies?.map((reply) =>
                  reply.id === parentId
                    ? {
                        ...reply,
                        replies: [...(reply.replies || []), newReply],
                      }
                    : reply
                ),
              }
        )
      );
    } catch (err) {
      const error = err as AxiosError;
      toast.error("Failed to post reply. " + error.message);
    }
  };

  const handleAddTopLevelComment = async (postId : number, text: string) => {
    try {
      const newComment = await createNewCommentForSpecificPost({post: postId, text: text});

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
            .filter(c => c.id !== id)
            .map(c => ({
              ...c,
              replies: c.replies ? removeCommentRecursively(c.replies) : undefined
            }));
        };
        
        return removeCommentRecursively(prev);
      });
    }
  } catch (err) {
    const error = err as AxiosError;
    toast.error("Failed to delete the comment. " + error.message);
  }
}


  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>

      <DialogContent className="min-w-[90%] h-[90vh] p-0 rounded-2xl overflow-hidden">
      <div className="flex flex-col md:flex-row w-full h-full min-h-0">
        {/* Left */}
        {post?.image && (
          <div className="md:w-1/2 w-full h-[40vh] md:h-full bg-black flex-shrink-0">
            <img
              src={post.image}
              alt={post.title}
              className="w-full h-full object-cover"
            />
          </div>
        )}

          <div className="md:w-1/2 w-full p-6 flex flex-col h-full min-h-0 overflow-y-auto">
            <div className="flex flex-col gap-4 flex-none">
              <div className="flex items-center gap-4">
                <CustomAvatar name={profile?.name} photo={profile?.photo} size={48} />
                <div>
                  <p className="font-bold">{profile?.name}</p>
                  <p className="text-muted-foreground text-sm">@{profile?.username}</p>
                </div>
              </div>

              <div>
                <h2 className="text-xl font-semibold">{post?.title}</h2>
                <p className="text-gray-700">{post?.desc}</p>
              </div>

              <CommentForm postId={post?.id || 0} onSubmit={handleAddTopLevelComment} />
            </div>

            <div className="mt-4 flex-1 min-h-0 pr-2">
              {comments?.length === 0 ? (
                <p className="text-sm text-muted-foreground">No comments yet.</p>
              ) : (
                comments?.map((comment) => (
                  <Comments
                    postId={post?.id || 0}
                    key={comment.id}
                    comment={comment}
                    addReply={handleAddReply}
                    deleteComment={handleDeleteComment}
                  />
                ))
              )}
              {commentPage && commentPage.next != null && (
                <div className="mt-2">
                  {loading 
                  ? <p 
                  className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400">
                    🔄 Loading...</p>
                  : <p 
                  className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400"
                  onClick={loadMoreComments}
                  >🚀 Load More</p>
                  }
                </div>
              )
              }
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
