import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { AuthDataForm } from "../auth/AuthDataForm";
import { useMyProfileDetail } from "@/hooks/useMyProfileDetail";
import { PersonalDataForm } from "./PersonalDataForm";

export const UserProfile = () => {

  const {profile, setProfile} = useMyProfileDetail();

  return (
    <div className="bg-white p-6 rounded-xl shadow border border-gray-200">
      <div className="flex items-center space-x-6 mb-6">
         <div className="w-24 h-24 rounded-full border flex items-center justify-center bg-gray-200">
          {profile?.photo ? (
            <img
              src={profile.photo}
              alt="Profile"
              className="w-full h-full rounded-full object-cover"
            />
          ) : (
            <span className="text-3xl font-bold text-gray-600">
              {profile?.name?.charAt(0).toUpperCase() || profile?.alias?.charAt(0).toUpperCase() || '?'}
            </span>
          )}
        </div>
        <div>
          <h2 className="text-2xl font-bold">{profile?.alias}</h2>
          <p className="text-sm text-gray-500">Joined on {profile?.createdAt}</p>
        </div>
      </div>

      {/* TODO: create followers/followees functionality */}
      
      <div className="space-y-4">
        <div>
          <Label>Email</Label>
          <Input value={profile?.email || ""} disabled className="mt-1" />
        </div>

        <div>
          <Label>Authentication Provider</Label>
          <Input value={profile?.authProvider || ""} disabled className="mt-1" />
        </div>
        
        <AuthDataForm email={profile?.email} alias={profile?.alias} authProvier={profile?.authProvider}/>
        
        <PersonalDataForm profile={profile} setProfile={setProfile}/>
      </div>
    </div>
  );
};