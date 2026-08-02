import { NavLink } from "react-router"
import { Button } from "@/components/ui/button"
import { CustomAvatar } from "./CustomAvatar"
import type { ProfilePage } from "@/types/Profile"
import { Pin } from "lucide-react"

export const ProfileList = (data: { profileList: ProfilePage}) => {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6 mb-5">
      {data.profileList?.content.length > 0 ? (
        data.profileList?.content.map((profile) => (
          <div
            key={profile.id}
            className="relative overflow-visible bg-[#F3EBD9] border border-[#C9A063] rounded-sm shadow-md hover:shadow-xl transition-all duration-300 p-4 pt-6"
          >
            <Pin
              className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 w-4 h-4 z-20 drop-shadow rotate-[-10deg]"
              style={{ color: "#D9A441" }}
              fill="#D9A441"
            />
            <div className="flex items-center space-x-4 mb-4">
              <CustomAvatar name={profile.name} photo={profile.photo} size={50} />
              <div>
                <h3 className="font-display font-semibold text-lg text-[#241F1A]">{profile.name}</h3>
                <p className="text-sm text-[#8A7F6C]">@{profile.username}</p>
              </div>
            </div>
            <NavLink to={`/profile/${profile.id}`}>
              <Button
                className="w-full mt-2 rounded-sm border-[#6B4A32] text-[#241F1A] bg-transparent hover:bg-[#B23A2E] hover:text-[#F3EBD9] hover:border-[#B23A2E]"
                variant="outline"
              >
                View Profile
              </Button>
            </NavLink>
          </div>
        ))
      ) : (
        <p className="font-hand text-lg text-[#8A7F6C]">no profiles found</p>
      )}
    </div>
  )
}