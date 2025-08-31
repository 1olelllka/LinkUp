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
} from "@/components/ui/sidebar"
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { NavLink, useNavigate } from "react-router"
import { Archive, ChevronUp, Link, MessageCircle, Search, Signpost, UserRoundPen } from "lucide-react";
import { useProfileStore } from "@/store/useProfileStore";
import { logout } from "@/services/authServices";
import { useAuthStore } from "@/store/useAuthStore";
import { Label } from "@/components/ui/label";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { toast } from "sonner";
import type { AxiosError } from "axios";


const items = [
  {
    title: "Feed",
    url: "/feeds",
    icon: Signpost
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
    icon: Archive
  },
  {
    title: "My Profile",
    url: "/profile",
    icon: UserRoundPen
  },
]

export function AppSidebar() {
  const { profile, clearProfile } = useProfileStore();
  const navigate = useNavigate();
  const {open} = useSidebar();

  return (
    <Sidebar collapsible="icon" variant="floating">
      <SidebarHeader>
          <SidebarMenu className="ml-1">
              {open ? (
              <SidebarMenuItem className="flex flex-row gap-2">
                  <Link className="border-1 rounded-md p-1"/>
                  <Label>LinkUP!</Label>
              </SidebarMenuItem>
              )
              : (
              <SidebarMenuItem>
                  <Link className="border-1 rounded-md p-1"/>
              </SidebarMenuItem>
              )
              }
          </SidebarMenu>
      </SidebarHeader>
      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Navigation</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              {items.map((item) => (
                <SidebarMenuItem key={item.title}>
                  <SidebarMenuButton asChild>
                    <NavLink to={item.url}>
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
                  <SidebarMenuButton>
                    <Avatar className="size-6">
                      <AvatarImage src="" />
                      <AvatarFallback>{profile?.alias.at(0)}</AvatarFallback>
                    </Avatar>
                    {profile?.alias || "Anonymous"}
                    <ChevronUp className="ml-auto" />
                  </SidebarMenuButton>
                </DropdownMenuTrigger>
                <DropdownMenuContent
                  side="top"
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
                  className="rounded"
                  >
                    <span>Sign out</span>
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </SidebarMenuItem>
          </SidebarMenu>
      </SidebarFooter>
    </Sidebar>
  )
}