import { AppSidebar } from "@/layouts/LeftSidebar"
import { Button } from "@/components/ui/button";
import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
import { ArrowLeft, ArrowRight } from "lucide-react";
import { useNavigate } from "react-router";

export function MainLayout({ children }: { children: React.ReactNode }) {
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
    </SidebarProvider>
  );
}
