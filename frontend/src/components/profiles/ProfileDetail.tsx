import { NavLink, useParams } from "react-router";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { CustomAvatar } from "./CustomAvatar";
import { useProfileDetail } from "@/hooks/useProfileDetail";
import { useFollowList } from "@/hooks/useFollowList";
import { UserPosts } from "../posts/UserPosts";
import { useProfileStore } from "@/store/useProfileStore";
import { checkFollowStatus, followProfile, unfollowProfile } from "@/services/profileServices";
import { useEffect, useState } from "react";

export function ProfileDetail() {
  const { userId } = useParams();
  const currentUserId = useProfileStore.getState().profile?.userId;
  const profile = useProfileDetail(userId);
  const [isFollowing, setIsFollowing] = useState<boolean>(false);
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
  

  useEffect(() => {
    const checkFollow = async () => {
      const followStatus = await checkFollowStatus(currentUserId, userId);
      if (followStatus == 200) {
        setIsFollowing(true);
      } else {
        setIsFollowing(false);
      }
    }
    checkFollow();
  }, [userId, currentUserId])


  return (
    <div>
      <Card className="p-6 flex flex-row justify-between items-center md:items-start gap-10 w-[99%] border-0 shadow-lg bg-slate-50 transition-all">
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
              {isFollowing ? (
                <>
                  <Button variant="outline" onClick={() => {
                    unfollowProfile(useProfileStore.getState().profile?.userId, userId)
                    .then(response => {
                      if (response == 200) {
                        setIsFollowing(false);
                      }
                    });
                  }}>Unfollow</Button>
                  <p className="text-xs text-muted-foreground">
                    You follow this user
                  </p>
                </>
              ) : (
                <Button onClick={() => {
                    followProfile(useProfileStore.getState().profile?.userId, userId)
                    .then(response => {
                      if (response == 200) {
                        setIsFollowing(true);
                      }
                    });
                }}>Follow</Button>
              )}
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
