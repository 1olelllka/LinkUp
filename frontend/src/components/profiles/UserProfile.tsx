import { useState } from "react";
import { useMyProfileDetail } from "@/hooks/useMyProfileDetail";
import { PersonalDataForm } from "./PersonalDataForm";
import { useProfileStore } from "@/store/useProfileStore";
import { useFollowList } from "@/hooks/useFollowList";
import { NavLink } from "react-router";
import { CustomAvatar } from "./CustomAvatar";
import { ServiceError } from "../errors/ServiceUnavailable";
import { Pin } from "lucide-react";

import { PageLoader } from "../load/PageLoader";
import { AvatarDialog } from "./AvatarDialog";

const ABOUT_ME_LIMIT = 150;

export const UserProfile = () => {
  const { profile, setProfile, error, loading } = useMyProfileDetail();
  const [aboutMeExpanded, setAboutMeExpanded] = useState(false);

  const followersPage = useFollowList({
    userId: useProfileStore.getState().profile?.id,
    type: "follower",
  });
  const followeesPage = useFollowList({
    userId: useProfileStore.getState().profile?.id,
    type: "followee",
  });

  const aboutMe = profile?.aboutMe ?? "";
  const isAboutMeLong = aboutMe.length > ABOUT_ME_LIMIT;
  const displayedAboutMe =
    isAboutMeLong && !aboutMeExpanded
      ? aboutMe.slice(0, ABOUT_ME_LIMIT).trimEnd()
      : aboutMe;

  return (
    <div className="relative bg-[#E8DFC8] border border-[#C9A063] p-6 md:p-8 rounded-sm shadow-lg transition-all w-[99%]">
      <Pin
        className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 w-5 h-5 z-10 drop-shadow rotate-[-8deg]"
        style={{ color: "#D9A441" }}
        fill="#D9A441"
      />
      {error ? (
        <ServiceError err={error} />
      ) : loading ? (
        <PageLoader />
      ) : (
        <>
          {/* Header */}
          <div className="flex items-start gap-6 md:gap-12 pb-2">
            {/* Avatar */}
            <div className="shrink-0">
              <AvatarDialog
                children={
                  <CustomAvatar
                    name={profile?.username}
                    photo={profile?.photo}
                    size={110}
                    className="cursor-pointer hover:opacity-80 transition-opacity"
                  />
                }
                setProfile={setProfile}
              />
            </div>

            {/* Identity + stats + bio */}
            <div className="flex-1 min-w-0 pt-2">
              <div className="flex items-center flex-wrap gap-4 mb-1">
                <h1 className="font-display text-xl font-bold text-[#241F1A] truncate">
                  {profile?.username}
                </h1>
                <PersonalDataForm profile={profile} setProfile={setProfile} />
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
                  to={`/profile/${useProfileStore.getState().profile?.id}/followers`}
                  className="flex items-baseline gap-1 hover:opacity-70 transition-opacity"
                >
                  <span className="text-sm font-semibold text-[#241F1A]">
                    {followersPage.followListPage?.totalElements ?? 0}
                  </span>
                  <span className="text-sm text-[#8A7F6C]">followers</span>
                </NavLink>
                <NavLink
                  to={`/profile/${useProfileStore.getState().profile?.id}/followees`}
                  className="flex items-baseline gap-1 hover:opacity-70 transition-opacity"
                >
                  <span className="text-sm font-semibold text-[#241F1A]">
                    {followeesPage.followListPage?.totalElements ?? 0}
                  </span>
                  <span className="text-sm text-[#8A7F6C]">following</span>
                </NavLink>
              </div>

              <p className="font-hand text-base text-[#8A7F6C]">
                joined {profile?.createdAt}
              </p>
            </div>
          </div>
        </>
      )}
    </div>
  );
};