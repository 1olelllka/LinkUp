import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog";
import { CustomAvatar } from "../profiles/CustomAvatar";
import { useEffect, useState } from "react";
import type { Post } from "@/types/Post";
import type { Profile } from "@/types/Profile";
import { getPostDetailsById } from "@/services/postServices";
import { getSpecificProfileInfo } from "@/services/profileServices";
// import { useComments } from "@/hooks/useComments";

export function PostModal({ postId, trigger }: { postId: number, trigger: React.ReactNode }) {
const [open, setOpen] = useState(false);

  // Fetch only when modal is open
  const [shouldFetch, setShouldFetch] = useState(false);
  const [post, setPost] = useState<Post>();
  const [profile, setProfile] = useState<Profile>();

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
        } catch (err) {
          console.error("Failed to fetch post/profile", err);
        }
      };

      if (shouldFetch && !post) {
        fetchData();
      }
    }, [shouldFetch, postId, post]);


  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent className="w-full max-w-4xl max-h-[90vh] overflow-y-auto p-6 rounded-2xl">
        <div className="space-y-4">
          {/* Image */}
          {post?.image && (
            <div className="w-[450px] h-[400px] rounded-xl overflow-hidden">
              <img
                src={post.image}
                alt={post.title}
                className="w-full h-full object-cover"
              />
            </div>
          )}

          {/* Post info */}
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

          {/* Comments */}
          {/* <div className="space-y-2">
            <h3 className="font-semibold text-lg">Comments</h3>
            {comments.length === 0 && (
              <p className="text-sm text-muted-foreground">No comments yet.</p>
            )}
            {comments.map((comment) => (
              <div key={comment.id} className="border p-3 rounded-lg">
                <div className="flex items-center gap-2 mb-1">
                  <CustomAvatar name={comment.user.name} photo={comment.user.photo} size={32} />
                  <div>
                    <p className="font-medium text-sm">{comment.user.name}</p>
                    <p className="text-xs text-muted-foreground">@{comment.user.username}</p>
                  </div>
                </div>
                <p className="text-sm text-gray-800">{comment.text}</p>

                {comment.replies?.length > 0 && (
                  <div className="pl-4 mt-2 space-y-1 border-l border-gray-200">
                    {comment.replies.map((reply: any) => (
                      <div key={reply.id} className="flex items-start gap-2">
                        <CustomAvatar name={reply.user.name} photo={reply.user.photo} size={28} />
                        <div>
                          <p className="text-sm font-medium">{reply.user.name}</p>
                          <p className="text-sm text-gray-700">{reply.text}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ))}
          </div> */}
        </div>
      </DialogContent>
    </Dialog>
  );
}
