import type { Post } from "@/types/Post";
import { useProfileDetail } from "@/hooks/useProfileDetail";
import { CustomAvatar } from "../profiles/CustomAvatar";
import { NavLink } from "react-router";
// import { FeedCommentForm } from "../feed/FeedCommentForm";
import { toast } from "sonner";
import { FeedCommentSheet } from "../feed/FeedCommentSheet";


export const PostCard = ({ id, user_id, desc, image, created_at }: Post) => {
  const {profile, detailError} = useProfileDetail(user_id);

  return (
    <>
      {detailError
      ? toast.error(detailError.message)
      : 
      <div className="bg-white rounded-xl p-4 shadow-md space-y-2">
        <div className="flex items-center space-x-3">
          <CustomAvatar name="feed avatar" photo={profile?.photo} size={60}/>
          <div>
            <h4 className="text-md font-semibold">{profile?.name}</h4>
            <NavLink to={`/profile/${profile?.id}`} >
              <h4 className="text-sm text-gray-400 font-bold hover:underline hover:cursor-pointer">@{profile?.username}</h4>
            </NavLink>
            <p className="text-xs text-gray-500">{new Date(created_at).toDateString()}</p>
          </div>
        </div>
        <p>{desc}</p>
        {image && (
          <div className="grid grid-cols-3 gap-2">
              <img
                key={id}
                src={image}
                className="rounded-lg object-cover h-32 w-full"
                alt="Post media"
              />
          </div>
        )}
        {/* <FeedCommentForm postId={id}/> */}
        <div className="flex space-x-4 text-sm text-gray-500 pt-2">
            <FeedCommentSheet children={
              <span className="cursor-pointer">💬 Comment</span>
            } postId={id}/>
        </div>
      </div>
      }
    </>
  );
};