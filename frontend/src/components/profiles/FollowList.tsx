import { useParams } from "react-router";
import { useFollowList } from "@/hooks/useFollowList";
import { ProfileList } from "./ProfileList";

interface FollowListProp {
  type: "follower" | "followee"
}

export function FollowList({type} : FollowListProp) {
  const { userId } = useParams();
  const followerPage = useFollowList({
    userId: userId,
    pageNumber: 0,
    type: type
  })

  return (
    <div className="bg-slate-50 rounded-2xl shadow-lg p-6 min-h-[calc(100vh-48px)] transition-all w-[99%]">
      <div className="max-w-4xl">
        <h1 className="text-3xl font-bold mb-6">{type === "followee" ? "Followees" : "Followers"}</h1>

        {followerPage && 
          <ProfileList profileList={followerPage} />
        }
      </div>
    </div>
  );
}
