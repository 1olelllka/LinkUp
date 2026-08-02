import type { Post } from "@/types/Post";
import { useProfileDetail } from "@/hooks/useProfileDetail";
import { CustomAvatar } from "../profiles/CustomAvatar";
import { NavLink } from "react-router";
// import { FeedCommentForm } from "../feed/FeedCommentForm";
import { toast } from "sonner";
import { FeedCommentSheet } from "../feed/FeedCommentSheet";
import { Pin } from "lucide-react";


export const PostCard = ({ id, user_id, desc, image, created_at }: Post) => {
  const {profile, detailError} = useProfileDetail(user_id);

  return (
    <>
      {detailError
      ? toast.error(detailError.message)
      : 
      <div className="relative overflow-visible bg-[#F3EBD9] border border-[#C9A063] rounded-sm shadow-md hover:shadow-xl transition-all duration-300 group p-4 pt-6 space-y-3">
        <Pin
          className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 w-4 h-4 z-20 drop-shadow rotate-[-10deg]"
          style={{ color: "#D9A441" }}
          fill="#D9A441"
        />

        <div className="flex items-center space-x-3">
          <CustomAvatar name="feed avatar" photo={profile?.photo} size={60}/>
          <div>
            <h4 className="font-display text-md font-semibold text-[#241F1A]">{profile?.name}</h4>
            <NavLink to={`/profile/${profile?.id}`} >
              <h4 className="text-sm text-[#8A7F6C] font-bold hover:text-[#B23A2E] hover:underline hover:cursor-pointer">@{profile?.username}</h4>
            </NavLink>
            <p className="text-xs text-[#8A7F6C]">{new Date(created_at).toDateString()}</p>
          </div>
        </div>
        <p className="text-[#4A4136]">{desc}</p>
        {image && (
          <div className="relative w-full h-48 rounded-sm overflow-hidden border border-[#C9A063]">
            <img
              key={id}
              src={image}
              className="w-full h-full object-cover transition duration-500 group-hover:scale-105"
              alt="Post media"
            />
          </div>
        )}
        {/* <FeedCommentForm postId={id}/> */}
        <div className="flex space-x-4 text-sm text-[#8A7F6C] pt-2 border-t border-[#C9A063]/60 mt-1">
            <FeedCommentSheet children={
              <span className="cursor-pointer hover:text-[#B23A2E] transition-colors pt-2 inline-block">💬 Comment</span>
            } postId={id}/>
        </div>
      </div>
      }
    </>
  );
};