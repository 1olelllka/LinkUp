import { useState, useRef } from "react";
import { ChatWindow } from "./ChatWindow";
import { useChatList } from "@/hooks/useChatList";
import { useProfileStore } from "@/store/useProfileStore";
import { Trash2, MessageCircle, Pin } from "lucide-react";
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
import { CustomAvatar } from "../profiles/CustomAvatar";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { PageLoader } from "../load/PageLoader";


type selectedChat = {
  id: string,
  selectedReceiverName: string,
  receiverId: string | undefined,
}

export const ChatList = () => {
  const [selectedChat, setSelectedChat] = useState<selectedChat | null>(null);
  const currentUserId = useProfileStore.getState().profile?.id;
  const { allChats, setAllChats, chatUsersPage, loadNextPage, loading, error, setRefresh } = useChatList(currentUserId, 0);
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

  if (error) {
    return (
      <div className="bg-[#E8DFC8] border border-[#C9A063] p-6 rounded-sm shadow-lg transition-all w-[99%] h-[94.5vh] overflow-hidden">
        <div className="flex items-center gap-3 mb-4">
          <Pin className="w-5 h-5 rotate-[-12deg] drop-shadow" style={{ color: "#D9A441" }} fill="#D9A441" />
          <h1 className="font-display text-2xl font-bold text-[#241F1A]">Messages</h1>
        </div>
        <ServiceError err={error} />
      </div>
    );
  }

  return (
    <div className="flex bg-[#E8DFC8] border border-[#C9A063] rounded-sm shadow-lg transition-all w-[99%] h-[94.5vh] overflow-hidden">
      {/* List pane — full width on mobile until a chat is picked, 1/3 on desktop always */}
      <div
        className={`w-full md:w-1/3 border-r border-[#C9A063] p-6 overflow-y-auto h-full ${selectedChat ? "hidden md:block" : "block"}`}
        onScroll={handleScroll}
        ref={chatListRef}
      >
        <div className="flex items-center gap-3 mb-4">
          <Pin className="w-5 h-5 rotate-[-12deg] drop-shadow" style={{ color: "#D9A441" }} fill="#D9A441" />
          <h2 className="font-display text-2xl font-bold text-[#241F1A]">Messages</h2>
        </div>

        <SearchNewChat selectedChat={selectedChat} setSelectedChat={setSelectedChat} searchTerm={searchTerm} setSearchTerm={setSearchTerm}/>

        {searchTerm.length == 0 &&
          <div className="space-y-2 mt-3">
            {allChats.map((chat) => {
              const other = chat.participants.find((p) => p.id !== currentUserId);
              console.log(other)
              const isActive = selectedChat?.id === chat.id;
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
                  className={`p-3 rounded-sm cursor-pointer transition flex items-center gap-3 border ${
                    isActive
                      ? "bg-[#DDD0B0] border-[#B23A2E]"
                      : "bg-[#F3EBD9] border-[#C9A063] hover:bg-[#DDD0B0]"
                  }`}
                >
                  <CustomAvatar name={other?.name} photo={other?.photo} size={40} />
                  <div className="flex-1 min-w-0">
                    <h4 className="font-display font-semibold text-sm text-[#241F1A] truncate">
                      {other?.name} <span className="font-normal text-[#8A7F6C]">@{other?.username}</span>
                    </h4>
                    <p className="text-xs text-[#8A7F6C] truncate">{chat.lastMessage}</p>
                  </div>
                  <div className="flex flex-col items-end gap-1 shrink-0">
                    <span className="text-[10px] text-[#8A7F6C]">
                      {new Date(chat.time).toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"})}
                    </span>
                    <Dialog
                      open={deleteDialogChatId === chat.id}
                      onOpenChange={(open) => setDeleteDialogChatId(open ? chat.id : null)}
                    >
                      <DialogTrigger asChild>
                        <button
                          onClick={(e) => e.stopPropagation()}
                          className="text-[#8A7F6C] hover:text-[#B23A2E] transition-colors p-0.5"
                          aria-label="Delete chat"
                        >
                          <Trash2 size={14} />
                        </button>
                      </DialogTrigger>
                      <DialogContent className="max-w-sm bg-[#E8DFC8] border-[#C9A063] rounded-sm">
                        <DialogTitle className="font-display text-xl font-bold text-[#241F1A]">Delete this chat?</DialogTitle>
                        <p className="text-sm text-[#4A4136]">
                          This chat will also be deleted for the other participant.
                        </p>
                        <DialogFooter className="sm:justify-end gap-2">
                          <DialogClose asChild>
                            <Button variant="outline" className="rounded-sm border-[#6B4A32] text-[#241F1A] hover:bg-[#DDD0B0]">
                              Cancel
                            </Button>
                          </DialogClose>
                          <DialogClose asChild>
                            <Button
                              className="rounded-sm bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9]"
                              onClick={() => {
                                deleteChatById(chat.id).then(response => {
                                  if (response.status == 204) {
                                    setSelectedChat(null);
                                    setAllChats((prev) => prev.filter((c) => c.id != chat.id))
                                    toast.success("Successfully deleted the chat!");
                                  } else {
                                    toast.warning("Unexpected response from server received: " + response.data);
                                  }
                                }).catch(err => toast.error((err as AxiosError).message));
                              }}
                            >
                              Delete
                            </Button>
                          </DialogClose>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                  </div>
                </div>
              );
            })}

            {loading && <PageLoader />}

            {chatUsersPage?.last && !loading && allChats.length > 0 && (
              <p className="font-hand text-base text-center text-[#8A7F6C] pt-2">you're all caught up</p>
            )}
            {!loading && allChats.length == 0 && (
              <div className="flex flex-col items-center justify-center py-10">
                <MessageCircle className="w-6 h-6 mb-2" style={{ color: "#D9A441" }} />
                <p className="font-hand text-lg text-[#8A7F6C]">start chatting with your friends</p>
              </div>
            )}
          </div>
        }
      </div>

      {/* Chat pane — hidden on mobile until a chat is picked, always visible on desktop */}
      <div className={`flex-1 p-6 overflow-y-auto h-full ${selectedChat ? "block" : "hidden md:block"}`}>
        {selectedChat ? (
          <ChatWindow
            chatId={selectedChat.id}
            senderId={currentUserId}
            receiverName={selectedChat.selectedReceiverName}
            receiverId={selectedChat.receiverId}
            setRefresh={setRefresh}
            allChats={allChats}
            setAllChats={setAllChats}
            onBack={() => setSelectedChat(null)}
          />
        ) : (
          <div className="flex items-center justify-center h-full">
            <p className="font-hand text-xl text-[#8A7F6C] text-center">select a chat to start reading</p>
          </div>
        )}
      </div>
    </div>
  );
};