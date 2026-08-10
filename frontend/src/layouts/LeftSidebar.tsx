import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { NavLink, useNavigate } from "react-router";
import {
  Archive,
  ChevronUp,
  Link,
  MessageCircle,
  Search,
  Signpost,
  UserRoundPen,
} from "lucide-react";
import { useProfileStore } from "@/store/useProfileStore";
import { logout } from "@/services/authServices";
import { useAuthStore } from "@/store/useAuthStore";
import { Label } from "@/components/ui/label";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { toast } from "sonner";
import type { AxiosError } from "axios";

const items = [
  {
    title: "Feed",
    url: "/feeds",
    icon: Signpost,
  },
  {
    title: "Messages",
    url: "/chats",
    icon: MessageCircle,
  },
  {
    title: "Search",
    url: "/search",
    icon: Search,
  },
  {
    title: "Archive",
    url: "/archive",
    icon: Archive,
  },
  {
    title: "My Profile",
    url: "/profile",
    icon: UserRoundPen,
  },
];

export function AppSidebar() {
  const { profile, clearProfile } = useProfileStore();
  const navigate = useNavigate();
  const { open } = useSidebar();

  return (
    <Sidebar collapsible="icon" variant="floating">
      <SidebarHeader>
        <SidebarMenu className="ml-1">
          {open ? (
            <SidebarMenuItem className="flex flex-row items-center gap-2">
              <Link className="border border-[#6B4A32] bg-[#C9A063] rounded-sm p-1 text-[#241F1A]" />
              <Label className="font-display font-bold text-[#241F1A]">LinkUP!</Label>
            </SidebarMenuItem>
          ) : (
            <SidebarMenuItem>
              <Link className="border border-[#6B4A32] bg-[#C9A063] rounded-sm p-1 text-[#241F1A]" />
            </SidebarMenuItem>
          )}
        </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel className="font-display text-xs uppercase tracking-wide text-[#8A7F6C]">
            Navigation
          </SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {items.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild className="hover:bg-[#DDD0B0]">
                    <NavLink
                      to={item.url}
                      className={({ isActive }) =>
                        isActive ? "text-[#B23A2E] font-semibold" : "text-[#241F1A]"
                      }
                    >
                      <item.icon />
                      <span>{item.title}</span>
                    </NavLink>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter className="overflow-visible">
        <SidebarMenu>
            <SidebarMenuItem>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <SidebarMenuButton className="hover:bg-[#DDD0B0] text-[#241F1A]">
                    {/* Intentionally put avatar image to '' to get fallback of the first letter */}
                    <Avatar className="size-6">
                      <AvatarImage src=''/>
                      <AvatarFallback className="bg-[#C9A063] text-[#241F1A] font-display font-bold">{profile?.username.at(0)}</AvatarFallback>
                    </Avatar>
                    {profile?.username || "Anonymous"}
                    <ChevronUp className="ml-auto" />
                  </SidebarMenuButton>
                </DropdownMenuTrigger>
                <DropdownMenuContent
                  side="top"
                  className="bg-[#F3EBD9] border-[#C9A063] rounded-sm"
                >
                  <DropdownMenuItem 
                    onClick={async () => {
                      logout().then(response => {
                      if (response.status == 200) {
                          useAuthStore.getState().clearToken();
                          clearProfile();
                          navigate("/login")
                      } else {
                          toast.error(`Unexpected error occured. Status - ${response.status}. Message - ${response.data.message}`)
                      }
                      })
                      .catch(err => {
                        const error = err as AxiosError;
                        toast.error(`Unexpected error occured. ${error.message}`)
                      });
                  }}
                  className="rounded-sm text-[#B23A2E] focus:bg-[#B23A2E] focus:text-[#F3EBD9]"
                  >
                    <span>Sign out</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </SidebarMenuItem>
          </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  );
}