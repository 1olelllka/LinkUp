import { ChatList } from "@/components/chat/ChatList";
import { ChatsLayout } from "@/layouts/ChatsLayout";


export const ChatsPage = () => {
  return (
      <ChatsLayout>
        <ChatList />
      </ChatsLayout>
  );
};