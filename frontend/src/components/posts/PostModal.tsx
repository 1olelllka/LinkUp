import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog";
import { CustomAvatar } from "../profiles/CustomAvatar";
import { useEffect, useState } from "react";
import type { Comment, Post } from "@/types/Post";
import type { Profile } from "@/types/Profile";
import { createNewCommentForSpecificPost, getAllCommentsForSpecificPost, getPostDetailsById } from "@/services/postServices";
import { getSpecificProfileInfo } from "@/services/profileServices";
import { Comments } from "./Comments";
import { CommentForm } from "./CommentForm";

export function PostModal({ postId, trigger }: { postId: number, trigger: React.ReactNode }) {
const [open, setOpen] = useState(false);

  // Fetch only when modal is open
  const [shouldFetch, setShouldFetch] = useState(false);
  const [post, setPost] = useState<Post>();
  const [profile, setProfile] = useState<Profile>();
  const [comments, setComments] = useState<Comment[]>();

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

          const commentData = await getAllCommentsForSpecificPost(postId);
          setComments(commentData.results);
        } catch (err) {
          console.error("Failed to fetch post/profile", err);
        }
      };

      if (shouldFetch && !post) {
        fetchData();
      }
    }, [shouldFetch, postId, post]);


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
      console.error("Failed to post reply", err);
      // optionally show toast
    }
  };

  const handleAddTopLevelComment = async (postId : number, text: string) => {
    try {
      const newComment = await createNewCommentForSpecificPost({post: postId, text: text});

      setComments((prev = []) => [...prev, newComment]);
    } catch (err) {
      console.log(err);
    }
  };


  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="w-full max-w-7xl max-h-[90vh] overflow-hidden p-0 rounded-2xl">
        <div className="flex flex-col md:flex-row w-full h-full">

          {/* Left: Image section */}
          {post?.image && (
            <div className="md:w-1/2 w-full h-[300px] md:h-auto bg-black">
              <img
                src={post.image}
                alt={post.title}
                className="w-full h-full object-cover"
              />
            </div>
          )}

          {/* Right: Content section */}
          <div className="md:w-1/2 w-full p-6 overflow-y-auto space-y-4">
            {/* Profile */}
            <div className="flex items-center gap-4">
              <CustomAvatar name={profile?.name} photo={profile?.photo} size={48} />
              <div>
                <p className="font-bold">{profile?.name}</p>
                <p className="text-muted-foreground text-sm">@{profile?.username}</p>
              </div>
            </div>

            {/* Post */}
            <div>
              <h2 className="text-xl font-semibold">{post?.title}</h2>
              <p className="text-gray-700">{post?.desc}</p>
            </div>

            {/* Comments */}
            <div className="space-y-2">
              <h3 className="font-semibold text-lg">Comments</h3>
              <CommentForm postId={post?.id || 0} onSubmit={handleAddTopLevelComment} />

              {comments?.length === 0 ? (
                <p className="text-sm text-muted-foreground">No comments yet.</p>
              ) : (
                comments?.map((comment) => (
                  <Comments
                    postId={post?.id || 0}
                    key={comment.id}
                    comment={comment}
                    addReply={handleAddReply}
                  />
                ))
              )}
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
