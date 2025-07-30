import type { Post } from "@/types/Post";
import { useProfileDetail } from "@/hooks/useProfileDetail";


export const PostCard = ({ id, user_id, desc, image, created_at }: Post) => {
  const profile = useProfileDetail(user_id);

  return (
    <div className="bg-white rounded-xl p-4 shadow-md space-y-2">
      <div className="flex items-center space-x-3">
        {/* TODO: ADD PHOTO */}
        <img src="/default_profile_photo.webp" alt="User" className="w-12 h-12 rounded-full" />
        <div>
          <h4 className="text-md font-semibold">{profile?.name}</h4>
          <h4 className="text-sm text-gray-400 font-bold hover:underline hover:cursor-pointer">@{profile?.username}</h4>
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
      <div className="flex space-x-4 text-sm text-gray-500 pt-2">
        <span>❤️ Like</span>
        <span>💬 Comment</span>
      </div>
    </div>
  );
};