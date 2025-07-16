import { SidebarLeft } from "@/components/SidebarLeft";
import { UserProfile } from "@/components/profiles/UserProfile";
import { UserPosts } from "@/components/profiles/UserPosts";

export const ProfilePage = () => {
  return (
    <div className="min-h-screen flex bg-[#f5f8ff] text-gray-800">
      {/* Sidebar */}
      <aside className="w-1/5 bg-white p-6 border-r border-gray-200">
        <SidebarLeft />
      </aside>

      {/* Main content */}
      <main className="flex-1 p-6 space-y-8 overflow-y-auto">
        <UserProfile />
        <UserPosts userId="2e4f028e-fe9a-413b-8f71-5c1ff6497ac8"/>
      </main>
    </div>
  );
};
