import { UserProfile } from "@/components/profiles/UserProfile";
import { UserPosts } from "@/components/posts/UserPosts";
import { ProfileLayout } from "@/layouts/ProfileLayout";
import { useProfileStore } from "@/store/useProfileStore";

export const ProfilePage = () => {
  return (
    <ProfileLayout>
      <UserProfile />
      <UserPosts userId={useProfileStore.getState().profile?.userId || ""}/>
    </ProfileLayout>
  );
};
