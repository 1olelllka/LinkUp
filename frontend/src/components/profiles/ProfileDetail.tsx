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
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import { Pin } from "lucide-react";

const ABOUT_ME_LIMIT = 150;

export function ProfileDetail() {
  const { userId } = useParams();
  const currentUserId = useProfileStore.getState().profile?.id;
  const {profile, detailError, detailLoading} = useProfileDetail(userId);
  const [isFollowing, setIsFollowing] = useState<boolean>(false);
  const [aboutMeExpanded, setAboutMeExpanded] = useState(false);
  const followers = useFollowList({
    userId,
    type: "follower",
  });
  const followee = useFollowList({
    userId,
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

  const aboutMe = profile?.aboutMe ?? "";
  const isAboutMeLong = aboutMe.length > ABOUT_ME_LIMIT;
  const displayedAboutMe =
    isAboutMeLong && !aboutMeExpanded
      ? aboutMe.slice(0, ABOUT_ME_LIMIT).trimEnd()
      : aboutMe;

  return (
    <div>
      {detailError
      ? <ServiceError err={detailError} />
      : 
      <>
        {detailLoading
          ? <PageLoader />
          : 
        <Card className="relative bg-[#E8DFC8] border border-[#C9A063] p-6 md:p-8 rounded-sm shadow-lg transition-all w-[99%]">
          <Pin
            className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 w-5 h-5 z-10 drop-shadow rotate-[-8deg]"
            style={{ color: "#D9A441" }}
            fill="#D9A441"
          />

          <div className="flex items-start gap-6 md:gap-12 pb-2">
            {/* Avatar */}
            <div className="shrink-0">
              <CustomAvatar name={profile?.name} photo={profile?.photo} size={110} />
            </div>

            {/* Identity + stats + bio */}
            <div className="flex-1 min-w-0 pt-2">
              <div className="flex items-center flex-wrap gap-4 mb-1">
                <h1 className="font-display text-xl font-bold text-[#241F1A] truncate">
                  {profile?.username}
                </h1>

                {currentUserId != userId && (
                  isFollowing ? (
                    <Button
                      variant="outline"
                      size="sm"
                      className="rounded-sm border-[#6B4A32] text-[#241F1A] bg-transparent hover:bg-[#DDD0B0]"
                      onClick={() => {
                        if (currentUserId && userId) {
                          unfollowProfile(currentUserId, userId)
                          .then(response => {
                            if (response.status == 200) {
                              setIsFollowing(false);
                              followers.setFollowerNumber(prev => prev - 1);
                            } else {
                              toast.warning("Unknown error occured. Please try again later. The request failed with status " + response.status);
                            }
                          }).catch(err => {
                            const error = err as AxiosError;
                            if (error.response && (error.response.status == 400 || error.response.status == 401)) {
                              toast.error((error.response.data as {message : string}).message);
                            } else {
                              toast.error((err as AxiosError).message);
                            }
                          });
                        }
                      }}
                    >
                      Unfollow
                    </Button>
                  ) : (
                    <Button
                      size="sm"
                      className="rounded-sm bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9]"
                      onClick={() => {
                        if (currentUserId && userId) {
                          followProfile(currentUserId, userId)
                          .then(response => {
                            if (response.status == 200) {
                              setIsFollowing(true);
                              followers.setFollowerNumber(prev => prev + 1);
                            } else {
                              toast.warning("Unknown error occured. Please try again later. The request failed with status " + response.status);
                            }
                          }).catch(err => {
                            const error = err as AxiosError;
                            if (error.response && (error.response.status == 400 || error.response.status == 401)) {
                              toast.error((error.response.data as {message : string}).message);
                            } else {
                              toast.error((err as AxiosError).message);
                            }
                          });
                        }
                      }}
                    >
                      Follow
                    </Button>
                  )
                )}
              </div>

              <p className="text-sm font-semibold text-[#241F1A] mb-2">
                {profile?.name}
              </p>

              {aboutMe && (
                <p className="text-sm text-[#4A4136] mb-2 whitespace-pre-wrap break-words">
                  {displayedAboutMe}
                  {isAboutMeLong && !aboutMeExpanded && "... "}
                  {isAboutMeLong && (
                    <button
                      type="button"
                      onClick={() => setAboutMeExpanded((prev) => !prev)}
                      className="text-[#8A7F6C] hover:text-[#B23A2E] transition-colors"
                    >
                      {aboutMeExpanded ? " less" : "more"}
                    </button>
                  )}
                </p>
              )}

              <div className="flex items-center gap-8 mb-2">
                <NavLink
                  to={`/profile/${userId}/followers`}
                  className="flex items-baseline gap-1 hover:opacity-70 transition-opacity"
                >
                  <span className="text-sm font-semibold text-[#241F1A]">
                    {followers.followerNumber}
                  </span>
                  <span className="text-sm text-[#8A7F6C]">followers</span>
                </NavLink>
                <NavLink
                  to={`/profile/${userId}/followees`}
                  className="flex items-baseline gap-1 hover:opacity-70 transition-opacity"
                >
                  <span className="text-sm font-semibold text-[#241F1A]">
                    {followee.followeeNumber}
                  </span>
                  <span className="text-sm text-[#8A7F6C]">following</span>
                </NavLink>
              </div>

              {isFollowing && (
                <p className="font-hand text-base text-[#8A7F6C]">you follow this user</p>
              )}

              {profile?.createdAt && (
                <p className="font-hand text-base text-[#8A7F6C]">
                  joined {profile.createdAt}
                </p>
              )}
            </div>
          </div>
        </Card>
        }
        <UserPosts userId={userId} />  
      </>
      }
    </div>
  );
}