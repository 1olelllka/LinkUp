import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AuthDataForm } from "../auth/AuthDataForm";
import { useMyProfileDetail } from "@/hooks/useMyProfileDetail";
import { PersonalDataForm } from "./PersonalDataForm";
import { useProfileStore } from "@/store/useProfileStore";
import { useFollowList } from "@/hooks/useFollowList";
import { NavLink } from "react-router";
import { CustomAvatar } from "./CustomAvatar";

export const UserProfile = () => {

  const {profile, setProfile} = useMyProfileDetail();
  const followersPage= useFollowList(
    {userId: useProfileStore.getState().profile?.userId, pageNumber: 0, type:"follower"}
  );
  const followeesPage = useFollowList({
    userId: useProfileStore.getState().profile?.userId,
    pageNumber: 0,
    type: "followee"
  });

  console.log(followersPage)

  return (
    <div className="bg-slate-50 p-6 rounded-xl shadow-lg transition-all w-[99%]">
      <div className="flex items-center justify-between flex-wrap gap-6 mb-6">
        <div className="flex items-center gap-6">
          <CustomAvatar name={profile?.name} photo={profile?.photo} size={84} />
          <div>
            <h2 className="text-2xl font-bold">{profile?.username}</h2>
            <p className="text-sm text-gray-500">Joined on {profile?.createdAt}</p>
          </div>
        </div>

        <div className="flex gap-8 text-center">
          <div>
            <NavLink to={`/profile/${useProfileStore.getState().profile?.userId}/followers`}>
              <p className="text-xl font-semibold cursor-pointer">{followersPage?.totalElements ?? 0}</p>
            </NavLink>
            <p className="text-sm text-muted-foreground">Followers</p>
          </div>
          <div>
            <NavLink to={`/profile/${useProfileStore.getState().profile?.userId}/followees`}>
              <p className="text-xl font-semibold cursor-pointer">{followeesPage?.totalElements ?? 0}</p>
            </NavLink>
            <p className="text-sm text-muted-foreground">Following</p>
          </div>
        </div>
      </div>

      <div className="space-y-4">
        <div>
          <Label>Email</Label>
          <Input value={profile?.email || ""} disabled className="mt-1" />
        </div>

        <div>
          <Label>Authentication Provider</Label>
          <Input value={profile?.authProvider || ""} disabled className="mt-1" />
        </div>
        
        <AuthDataForm email={profile?.email} alias={profile?.username} authProvier={profile?.authProvider}/>
        
        <PersonalDataForm profile={profile} setProfile={setProfile}/>
      </div>
    </div>
  );
};