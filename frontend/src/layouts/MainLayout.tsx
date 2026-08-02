import { AppSidebar } from "@/layouts/LeftSidebar"
import { Button } from "@/components/ui/button";
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
import { ArrowLeft, ArrowRight, Bell } from "lucide-react";
import { useNavigate } from "react-router";
import { NotificationSheet } from "@/components/notification/NotificationSheet";


export function MainLayout({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
  return (
    <SidebarProvider className="bg-[#1E1A16]">
      <AppSidebar />
      <main className="transition-all flex-1">
        <div className="flex flex-row">
          <SidebarTrigger className="mt-3 text-[#F3EBD9] hover:bg-[#2A241E] hover:text-[#D9A441]"/>
          <NotificationSheet trigger={
            <Button
              variant={"ghost"}
              size="icon"
              className="size-7 mt-3 text-[#F3EBD9] hover:bg-[#2A241E] hover:text-[#D9A441]"
            >
              <Bell />
            </Button>
          }/>
          <Button
            variant="ghost"
            size="icon"
            className="size-7 mt-3 text-[#F3EBD9] hover:bg-[#2A241E] hover:text-[#D9A441]"
            onClick={() => navigate(-1)}
          >
            <ArrowLeft />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="size-7 mt-3 text-[#F3EBD9] hover:bg-[#2A241E] hover:text-[#D9A441]"
            onClick={() => navigate(1)}
          >
            <ArrowRight />
          </Button>

        </div>
        {children}
      </main>
    </SidebarProvider>
  );
}