import { Feed } from "@/components/Feed";
import { SidebarLeft } from "@/components/SidebarLeft";
import { SidebarRight } from "@/components/SidebarRight";


export const FeedPage = () => {
    return (
    <div className="min-h-screen flex bg-[#f5f8ff] text-gray-800">
      <aside className="w-1/5 bg-white p-6 border-r border-gray-200">
        <SidebarLeft />
      </aside>

      <main className="flex-1 p-6 overflow-y-auto space-y-4">
        <h2 className="text-2xl font-bold mb-4">Feeds</h2>
        <Feed />
      </main>

      <aside className="w-1/4 bg-white p-6 border-l border-gray-200">
        <SidebarRight />
      </aside>
    </div>
  );

}