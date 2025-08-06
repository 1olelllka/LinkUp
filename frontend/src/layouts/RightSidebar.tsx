import { Label } from "@/components/ui/label"
import {
  Sidebar,
  SidebarContent,
} from "@/components/ui/sidebar"


export function RightSidebar() {

  return (
    <Sidebar collapsible="none" variant="floating" side="right" className="bg-slate-200 rounded-xl max-h-[calc(100vh-60vh)] m-2">
      <SidebarContent className="p-3 rounded-xl">
        <Label className="text-2xl font-semibold">Stories</Label>
        <div className="flex space-x-2 overflow-x-auto">
          <img src="/story_img.jpg" className="w-25 h-40 rounded-xl hover:cursor-pointer hover:opacity-75" />
          <img src="/story_img.jpg" className="w-25 h-40 rounded-xl hover:cursor-pointer hover:opacity-75" />
        </div>
      </SidebarContent>
    </Sidebar>
  )
}