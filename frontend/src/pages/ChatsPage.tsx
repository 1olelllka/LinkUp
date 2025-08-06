import { ChatList } from "@/components/chat/ChatList";
import { MainLayout } from "@/layouts/MainLayout";


export const ChatsPage = () => {
  return (
      <MainLayout>
        <ChatList />
      </MainLayout>
  );
};