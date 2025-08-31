import { useState, useRef } from "react";
import { ChatWindow } from "./ChatWindow";
import { useChatList } from "@/hooks/useChatList";
import { useProfileStore } from "@/store/useProfileStore";
import { Eraser } from "lucide-react";
import {
  HoverCard,
  HoverCardContent,
  HoverCardTrigger,
} from "@/components/ui/hover-card"
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { DialogClose } from "@radix-ui/react-dialog";
import { Button } from "../ui/button";
import { deleteChatById } from "@/services/chatServices";
import { SearchNewChat } from "./SearchNewChat";
import { ServiceError } from "../errors/ServiceUnavailable";


type selectedChat = {
  id: string,
  selectedReceiverName: string,
  receiverId: string | undefined,
}

export const ChatList = () => {
  const [selectedChat, setSelectedChat] = useState<selectedChat | null>(null);
  const currentUserId = useProfileStore.getState().profile?.userId;
  const { allChats, setAllChats, chatUsersPage, loadNextPage, loading, error } = useChatList(currentUserId, 0);
  const [deleteDialogChatId, setDeleteDialogChatId] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState<string>("");

  const chatListRef = useRef<HTMLDivElement>(null);

  const handleScroll = () => {
    const el = chatListRef.current;
    if (!el) return;

    if (el.scrollHeight - el.scrollTop - el.clientHeight < 100) {
      loadNextPage();
    }
  };

  return (
    <>
    {error 
      ? 
      <div className="bg-slate-50 p-6 rounded-xl shadow-lg transition-all w-[99%] h-[94.5vh] overflow-hidden">
        <h1 className="text-3xl font-bold">Chats</h1>
        <ServiceError err={error} />
      </div>
      : 
      <div 
        className="flex bg-slate-50 p-6 rounded-xl shadow-lg transition-all w-[99%] h-[94.5vh] overflow-hidden"    
        >
              <div
                className="w-1/3 border-r pr-4 overflow-y-auto h-full"
                onScroll={handleScroll}
                ref={chatListRef}
              >
                <h2 className="text-xl font-bold mb-4">Messages</h2>
                <SearchNewChat selectedChat={selectedChat} setSelectedChat={setSelectedChat} searchTerm={searchTerm} setSearchTerm={setSearchTerm}/>
                {searchTerm.length == 0 &&         
                  <div className="space-y-3">
                    {allChats.map((chat) => {
                      const other = chat.participants.find((p) => p.id !== currentUserId);
                      return (
                        <div
                          key={chat.id}
                          onClick={() =>
                            setSelectedChat({
                              id: chat.id,
                              selectedReceiverName: other ? other.name : "",
                              receiverId: other?.id
                            })
                          }
                          className={`p-4 rounded-xl cursor-pointer transition flex justify-between items-center ${
                            selectedChat?.id === chat.id ? "bg-gray-200" : "hover:bg-gray-100"
                          }`}
                        >
                          <div>
                            <h4 className="font-semibold">{other?.name} (@{other?.username})</h4>
                            <p className="text-sm text-gray-500 truncate w-40">Dummy message</p>
                          </div>
                          <div>
                            <Dialog 
                              open={deleteDialogChatId === chat.id} 
                              onOpenChange={(open) => setDeleteDialogChatId(open ? chat.id : null)}
                            >
                              <DialogTrigger asChild>
                                <div onClick={(e) => e.stopPropagation()}>
                                  <HoverCard>
                                    <HoverCardTrigger asChild>
                                      <Eraser size={15} className="cursor-pointer justify-self-end" />
                                    </HoverCardTrigger>
                                    <HoverCardContent className="w-20 p-1">
                                      <p className="text-xs text-slate-500">Delete Chat</p>
                                    </HoverCardContent>
                                  </HoverCard>
                                </div>
                              </DialogTrigger>
                              <DialogContent className="w-100">
                                <DialogTitle>Warning</DialogTitle>
                                <p className="text-md">Are you sure you want to delete this chat? 
                                <p className="text-red-500 text-xs">*This chat will also be deleted for other participant!</p></p>
                                <DialogFooter className="sm:justify-end">
                                    <DialogClose asChild>
                                      <Button type="button" variant="secondary">
                                        Close
                                      </Button>
                                    </DialogClose>
                                    <DialogClose asChild>
                                      <Button 
                                        type="button" 
                                        variant="destructive"
                                        onClick={() => {
                                          deleteChatById(chat.id).then(response => {
                                            if (response.status == 204) {
                                              setSelectedChat(null);
                                              setAllChats((prev) => prev.filter((c) => c.id != chat.id))
                                            } else {
                                              console.log("Unexpected response status -> " + response);
                                            }
                                          }).catch(err => console.log(err));
                                        }}>
                                        Delete
                                      </Button>
                                    </DialogClose>
                                </DialogFooter>
                              </DialogContent>
                            </Dialog>
                            <span className="text-xs text-gray-400">11:53</span>
                          </div>
                        </div>
                      );
                    })}

                    {loading && (
                      <p className="font-semibold text-center text-slate-400">🔄 Loading...</p>
                    )}

                    {chatUsersPage?.last && !loading && allChats.length > 0 && (
                      <p className="font-semibold text-center text-slate-400">🚀 You're all caught up!</p>
                    )}
                    {allChats.length == 0 && (
                      <p className="font-semibold text-center text-slate-400 mt-10">💬 Start chatting with your friends!</p>
                    )}
                  </div>
                }
              </div>

              <div className="flex-1 pl-6 overflow-y-auto h-full">
                {selectedChat ? (
                  <ChatWindow
                    chatId={selectedChat.id}
                    senderId={currentUserId}
                    senderName={selectedChat.selectedReceiverName}
                    receiverId={selectedChat.receiverId}
                  />
                ) : (
                  <div className="flex items-center justify-self-center h-[90%] fixed">
                    <p className="text-gray-400 text-center">Select a chat to view conversation</p>
                  </div>
                )}
              </div>
      </div>
    }
    </>
  );
};