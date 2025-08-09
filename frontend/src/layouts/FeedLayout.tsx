
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar";
// import { SidebarRight } from "@/layouts/SidebarRight";
import { type ReactNode } from "react";
import { useNavigate } from "react-router";
import { AppSidebar } from "./LeftSidebar";
import { Button } from "@/components/ui/button";
import { ArrowLeft, ArrowRight } from "lucide-react";
import { RightSidebar } from "./RightSidebar";

export const FeedLayout = ({ children }: { children: ReactNode }) => {
  const navigate = useNavigate();
  return (
    <SidebarProvider>
      <AppSidebar />
      <main className="transition-all flex-1">
        <div className="flex flex-row">
          <SidebarTrigger className="mt-3"/>
              <Button
                data-sidebar="trigger"
                data-slot="sidebar-trigger"
                variant="ghost"
                size="icon"
                className="size-7 mt-3"
                onClick={() => navigate(-1)}
              >
              <ArrowLeft />
            </Button>
              <Button
                data-sidebar="trigger"
                data-slot="sidebar-trigger"
                variant="ghost"
                size="icon"
                className="size-7 mt-3"
                onClick={() => navigate(1)}
              >
                <ArrowRight />
              </Button>
        </div>
        {children}
      </main>
      <RightSidebar />
    </SidebarProvider>
  );
}



//   <div className="min-h-screen flex bg-[#f5f8ff] text-gray-800">
//     <aside className="w-1/5 bg-white p-6 border-r border-gray-200">
//       <SidebarLeft />
//     </aside>

//     <main className="flex-1 p-6 overflow-y-auto space-y-4">
//       {children}
//     </main>

//     <aside className="w-1/4 bg-white p-6 border-l border-gray-200">
//       <SidebarRight />
//     </aside>
//   </div>
// );