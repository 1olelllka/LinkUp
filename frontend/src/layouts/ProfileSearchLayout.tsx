import { SidebarLeft } from "@/layouts/SidebarLeft";
import type { ReactNode } from "react";

export const ProfileSearchLayout = ({children} : {children : ReactNode}) => {
  return (
    <div className="min-h-screen flex bg-[#f5f8ff] text-gray-800">
      {/* Sidebar */}
      <aside className="w-1/5 bg-white p-6 border-r border-gray-200">
        <SidebarLeft />
      </aside>

        <main className="flex-1 p-6 overflow-y-auto">
            {children}
        </main>
    </div>
    );
}