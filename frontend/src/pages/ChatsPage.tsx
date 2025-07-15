import { ChatList } from "@/components/ChatList";
import { SidebarLeft } from "@/components/SidebarLeft";


export const ChatsPage = () => {
  return (
    <div className="min-h-screen flex bg-[#f5f8ff] text-gray-800">
      {/* Sidebar */}
      <aside className="w-1/5 bg-white p-6 border-r border-gray-200">
        <SidebarLeft />
      </aside>

      {/* Full chat area now */}
      <main className="flex-1 bg-white p-6 overflow-hidden">
        <ChatList />
      </main>
    </div>
  );
};