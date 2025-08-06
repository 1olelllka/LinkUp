import { UserProfile } from "@/components/profiles/UserProfile";
import { UserPosts } from "@/components/posts/UserPosts";
import { useProfileStore } from "@/store/useProfileStore";
import { MainLayout } from "@/layouts/MainLayout";

export const ProfilePage = () => {
  return (
    <MainLayout>
      <UserProfile />
      <UserPosts userId={useProfileStore.getState().profile?.userId || ""}/>
    </MainLayout>
  );
};
