import { UserProfile } from "@/components/profiles/UserProfile";
import { UserPosts } from "@/components/profiles/UserPosts";
import { ProfileLayout } from "@/layouts/ProfileLayout";

export const ProfilePage = () => {
  return (
    <ProfileLayout>
      <UserProfile />
      <UserPosts userId="436c5a79-ee35-4995-86d1-475e3a14d584"/>
    </ProfileLayout>
  );
};
