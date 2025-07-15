import { SidebarLeft } from "@/components/SidebarLeft";
import { ProfileSearch } from "@/components/ProfileSearch";

export const ProfileSearchPage = () => {
  return (
    <div className="min-h-screen flex bg-[#f5f8ff] text-gray-800">
      {/* Sidebar */}
      <aside className="w-1/5 bg-white p-6 border-r border-gray-200">
        <SidebarLeft />
      </aside>

      {/* Main content */}
      <main className="flex-1 p-6 overflow-y-auto">
        <ProfileSearch />
      </main>
    </div>
  );
};
