import type { ReactNode } from "react";
import { SidebarLeft } from "./SidebarLeft";


export const ChatsLayout = ({children} : {children : ReactNode} ) => {
    return (
        <div className="min-h-screen flex bg-[#f5f8ff] text-gray-800">
            {/* Sidebar */}
            <aside className="w-1/5 bg-white p-6 border-r border-gray-200">
                <SidebarLeft />
            </aside>

        <main className="flex-1 bg-white p-6 overflow-hidden">
            {children}
        </main>
    </div>
    )
}