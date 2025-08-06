import { useState } from "react";
import { ChatWindow } from "./ChatWindow";
import { useChatList } from "@/hooks/useChatList";
import { useProfileStore } from "@/store/useProfileStore";


type selectedChat = {
  id: string,
  selectedReceiverName: string,
}

export const ChatList = () => {
  const [selectedChat, setSelectedChat] = useState<selectedChat | null>(null);

  const currentUserId = useProfileStore.getState().profile?.userId;

  const chatUsers = useChatList(currentUserId);

  return (
    <div className="flex bg-slate-50 p-6 rounded-xl shadow-lg transition-all w-[99%] h-[93.6%]">
      <div className="w-1/3 border-r pr-4 overflow-y-auto">
        <h2 className="text-xl font-bold mb-4">Messages</h2>
        <div className="space-y-3">
          {chatUsers?.map((chat) => {
            const other = chat.participants.find((p) => p.id !== currentUserId);
            return (
              <div
                key={chat.id}
                onClick={() => setSelectedChat({"id": chat.id, "selectedReceiverName": other ? other.name : ""})}
                className={`p-4 rounded-xl cursor-pointer transition flex justify-between items-center ${
                  selectedChat?.id === chat.id ? "bg-gray-200" : "hover:bg-gray-100"
                }`}
              >
                <div>
                  <h4 className="font-semibold">{other?.name}</h4>
                  <p className="text-sm text-gray-500 truncate w-40">Dummy message</p>
                </div>
                <span className="text-xs text-gray-400">11:53</span>
              </div>
            );
          })}
        </div>
      </div>

      {/* ChatWindow (right column) */}
      <div className="flex-1 pl-6 overflow-y-auto">
        {selectedChat ? (
          <ChatWindow chatId={selectedChat.id} senderId={currentUserId} senderName={selectedChat.selectedReceiverName} />
        ) : (
          <div className="text-gray-400 flex items-center justify-center h-full">
            <p>Select a chat to view conversation</p>
          </div>
        )}
      </div>
    </div>
  );
};
