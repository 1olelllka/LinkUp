import { NavLink } from "react-router"
import { Button } from "@/components/ui/button"
import { CustomAvatar } from "./CustomAvatar"
import type { ProfilePage } from "@/types/Profile"

export const ProfileList = (data: { profileList: ProfilePage}) => {
    
    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {data.profileList?.content.length > 0 ? (
            data.profileList?.content.map((profile) => (
              <div
                key={profile.id}
                className="bg-white p-4 rounded-xl shadow hover:shadow-md transition border border-gray-200"
              >
                <div className="flex items-center space-x-4 mb-4">
                  <CustomAvatar name={profile.name} photo={profile.photo} size={50} />
                  <div>
                    <h3 className="font-semibold text-lg">{profile.name}</h3>
                    <p className="text-sm text-gray-500">@{profile.username}</p>
                  </div>
                </div>
                <NavLink to={`/profile/${profile.id}`}>
                  <Button className="w-full mt-2" variant="outline">
                    View Profile
                  </Button>
                </NavLink>
              </div>
            ))
          ) : (
            <p className="text-gray-500">No profiles found.</p>
          )}
        </div>
    )
}