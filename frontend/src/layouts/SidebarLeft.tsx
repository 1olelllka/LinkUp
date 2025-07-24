import { useProfileStore } from "@/store/useProfileStore";
import { NavLink, useLocation } from "react-router";


export const SidebarLeft = () => {

  const navItems = [
    { label: "Feed", path: "/feeds" },
    { label: "Messages", path: "/chats" },
    { label: "Search", path: "/search"},
    { label: "My Profile", path: "/profile" },
    { label: "Settings", path: "/settings" },
  ];
  const location = useLocation();

  return (
    <div className="space-y-6">
      <div className="flex items-center space-x-3">
        <img
          src="/avatar.jpg"
          alt="User avatar"
          className="w-12 h-12 rounded-full"
        />
        <div>
          <h4 className="font-bold text-lg">{useProfileStore.getState().profile?.alias || "Anonymous"}</h4>
        </div>
      </div>

      <nav className="space-y-2">
        {navItems.map((item, i) => (
          <NavLink to={item.path} key={i} className={`block w-full text-left px-4 py-2 rounded-lg hover:bg-gray-500 ${
                item.path === location.pathname ? "bg-black text-white font-semibold" : ""
              }`}>
            <span>
              {item.label}
            </span>
          </NavLink>
        ))}
      </nav>
    </div>
  );
}