import { NavLink, useParams } from "react-router";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { CustomAvatar } from "./CustomAvatar";
import { useProfileDetail } from "@/hooks/useProfileDetail";
import { useFollowList } from "@/hooks/useFollowList";
import { UserPosts } from "./UserPosts";
import { useProfileStore } from "@/store/useProfileStore";

export function ProfileDetail() {
  const { userId } = useParams();
  const profile = useProfileDetail(userId);
  const followers = useFollowList({
    userId,
    pageNumber: 0,
    type: "follower",
  });
  const followee = useFollowList({
    userId,
    pageNumber: 0,
    type: "followee",
  });


  return (
    <div className="p-6 max-w-5xl w-full mx-auto space-y-6">
      <Card className="p-6 flex flex-col md:flex-row justify-between items-center md:items-start gap-6 md:gap-10">
        {/* Left side: profile info */}
        <div className="flex-1 space-y-3 text-center md:text-left">
          <div>
            <h1 className="text-2xl font-bold">{profile?.name}</h1>
            <p className="text-muted-foreground">@{profile?.username}</p>
          </div>

          {profile?.aboutMe && (
            <p className="text-sm text-muted-foreground">{profile.aboutMe}</p>
          )}

          <div className="flex justify-center md:justify-start gap-8 pt-2">
            <div className="text-center">
              <NavLink to={`/profile/${userId}/followers`}>
                <p className="text-lg font-bold">{followers?.totalElements}</p>
                <p className="text-sm text-muted-foreground">Followers</p>
              </NavLink>
            </div>
            <div className="text-center">
              <NavLink to={`/profile/${userId}/followees`}>
                <p className="text-lg font-bold">{followee?.totalElements}</p>
                <p className="text-sm text-muted-foreground">Following</p>
              </NavLink>
            </div>
          </div>

          {/* Follow/unfollow button and status */}
          {useProfileStore.getState().profile?.userId != userId && (
            <div className="pt-4 space-y-2">
              {/* {profile?.isFollowedByCurrentUser ? (
                <>
                  <Button variant="outline">Unfollow</Button>
                  <p className="text-xs text-muted-foreground">
                    You follow this user
                  </p>
                </>
              ) : ( */}
                <Button>Follow</Button>
              {/* // )} */}
            </div>
          )}
        </div>

        {/* Right side: avatar */}
        <div className="shrink-0">
          <CustomAvatar name={profile?.name} photo={profile?.photo} size={100} />
        </div>
      </Card>

      <UserPosts userId={userId} />
    </div>
  );
}
