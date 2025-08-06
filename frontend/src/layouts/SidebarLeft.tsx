import { Button } from "@/components/ui/button";
import { logout } from "@/services/authServices";
import { useAuthStore } from "@/store/useAuthStore";
import { useProfileStore } from "@/store/useProfileStore";
import { NavLink, useLocation, useNavigate } from "react-router";

export const SidebarLeft = () => {
  const location = useLocation();
  const { profile, clearProfile } = useProfileStore();
  const navigate = useNavigate();

  const navItems = [
    { label: "Feed", path: "/feeds" },
    { label: "Messages", path: "/chats" },
    { label: "Search", path: "/search" },
    { label: "My Profile", path: "/profile" },
  ];

  return (
    <div className="flex flex-col justify-between h-full p-4">
      {/* Top section with avatar and nav */}
      <div className="space-y-6">
        <div className="flex items-center space-x-3">
          <img
            src="/avatar.jpg"
            alt="User avatar"
            className="w-12 h-12 rounded-full"
          />
          <div>
            <h4 className="font-bold text-lg">
              {profile?.alias || "Anonymous"}
            </h4>
          </div>
        </div>

        <nav className="space-y-2">
          {navItems.map((item, i) => (
            <NavLink
              to={item.path}
              key={i}
              className={`block w-full text-left px-4 py-2 rounded-lg hover:bg-gray-500 ${
                item.path === location.pathname
                  ? "bg-black text-white font-semibold"
                  : ""
              }`}
            >
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      <div className="flex justify-start mt-5">
        <Button
          onClick={async () => {
            logout().then(response => {
              if (response.status == 200) {
                useAuthStore.getState().clearToken();
                clearProfile();
                navigate("/login")
              } else {
                console.log("Unexpected status --> " + response);
              }
            }).catch(err => console.log(err));
          }}
          variant={"destructive"}
          className="rounded hover:bg-red-700"
        >
          Logout
        </Button>
      </div>
    </div>
  );
};
