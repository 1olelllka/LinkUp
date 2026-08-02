import { useChatWebSocket } from "@/hooks/useChatWebSocket";
import { useMessageList } from "@/hooks/useMessageList";
import { useEffect, useState, useRef, useCallback } from "react";
import {
  ContextMenu,
  ContextMenuContent,
  ContextMenuItem,
  ContextMenuTrigger,
} from "@/components/ui/context-menu";
import {
  deleteSpecificMessageById,
  updateMessageById,
} from "@/services/chatServices";
import { ObjectId } from "bson";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import type { ChatListResponse } from "@/types/Chat";
import { ArrowLeft, X, Send } from "lucide-react";

type ChatWindowProps = {
  chatId: string;
  senderId: string | undefined;
  receiverId: string | undefined;
  receiverName: string;
  setRefresh: (page: number) => void;
  allChats: ChatListResponse[];
  setAllChats: React.Dispatch<React.SetStateAction<ChatListResponse[]>>;
  onBack?: () => void;
};

export const ChatWindow = ({
  chatId,
  senderId,
  receiverName,
  receiverId,
  setRefresh,
  allChats,
  setAllChats,
  onBack,
}: ChatWindowProps) => {
  const { connectionStatus, lastMessage, sendMessage } = useChatWebSocket(
    senderId,
    receiverId
  );
  const {
    messagesPage,
    messages,
    setMessages,
    loadMoreMessages,
    loading,
    error,
  } = useMessageList(chatId);
  const [message, setMessage] = useState("");
  const [updateId, setUpdateId] = useState<string | null>(null);

  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);
  const previousScrollHeight = useRef<number>(0);

  const maintainScrollPosition = useCallback(() => {
    if (messagesContainerRef.current && previousScrollHeight.current > 0) {
      const container = messagesContainerRef.current;
      const newScrollHeight = container.scrollHeight;
      const scrollDifference = newScrollHeight - previousScrollHeight.current;
      container.scrollTop = container.scrollTop + scrollDifference;
    }
  }, []);

  useEffect(() => {
    if (messages.length > 0 && previousScrollHeight.current > 0) {
      setTimeout(() => {
        maintainScrollPosition();
        previousScrollHeight.current = 0;
      }, 100);
    }
  }, [messages.length, maintainScrollPosition]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (messages && messages.length > 0) {
      setTimeout(scrollToBottom, 100);
    }
  }, [messages, messages?.length]);

  useEffect(() => {
    if (lastMessage) {
      setMessages((prev) => (prev ? [...prev, lastMessage] : [lastMessage]));
    }
  }, [lastMessage, setMessages]);

  const handleSendMessage = () => {
    if (message.trim() && senderId && receiverId) {
      const messageToSend = {
        id: new ObjectId().toString(),
        chatId,
        to: receiverId,
        from: senderId,
        content: message.trim(),
        createdAt: new Date().toISOString(),
      };
      setMessages((prev) => [...prev, messageToSend]);
      sendMessage(JSON.stringify(messageToSend));
      const foundChat = allChats.find((chat) => chat.id == chatId);
      if (!foundChat) {
        // trigger update list of chats if such chat does not exist
        // if chat exists on another page which wasn't fetched yet, it'll
        // be moved to the top of list by backend
        setRefresh(Math.random() * 1000);
      } else {
        const newChat = {...foundChat, lastMessage: messageToSend.content}
        setAllChats((prev) => [newChat, ...(prev.filter(c => c != foundChat))])
      }
      setMessage("");
    }
  };

  const handleUpdateMessage = async () => {
    if (message.trim() && updateId != null) {
      const messageToSend = {
        content: message.trim(),
      };
      try {
        const res = await updateMessageById(updateId, messageToSend);
        setMessages((prev) =>
          prev.map((obj) => (obj.id === updateId ? { ...obj, ...res } : obj))
        );
        const foundChat = allChats.find(chat => chat.id == chatId);
        const lastMsg = messages[messages.length - 1].id == updateId;
        if (!foundChat) {
          setRefresh(Math.random() * 1000);
        } else if (foundChat && lastMsg){
          const newChat = {...foundChat, lastMessage: messageToSend.content}
          setAllChats((prev) => [newChat, ...(prev.filter(c => c != foundChat))])
        }
        setUpdateId(null);
      } catch (err) {
        const error = err as AxiosError;
        if (error.status == 401 || error.status == 404) {
          toast.error((error.response?.data as { message: string }).message);
        } else {
          toast.error(error.message);
        }
      }
      setMessage("");
    }
  };

  const handleLoadMoreMessages = useCallback(async () => {
    if (
      !messagesPage ||
      messagesPage.pageable.pageNumber >= messagesPage.totalPages - 1
    ) {
      return;
    }

    if (messagesContainerRef.current) {
      previousScrollHeight.current = messagesContainerRef.current.scrollHeight;
    }

    const nextPage = messagesPage.pageable.pageNumber + 1;
    await loadMoreMessages(nextPage);
  }, [messagesPage, loadMoreMessages]);

  const isConnected = connectionStatus === "Open";

  return (
    <div className="flex flex-col h-full max-h-[90vh]">
      <div className="mb-4 flex items-center gap-3 pb-3 border-b border-[#C9A063]">
        {onBack && (
          <button
            onClick={onBack}
            className="md:hidden text-[#8A7F6C] hover:text-[#B23A2E] transition-colors"
            aria-label="Back to chats"
          >
            <ArrowLeft size={20} />
          </button>
        )}
        <div>
          <h2 className="font-display text-xl font-bold text-[#241F1A]">{receiverName}</h2>
          <p className="text-xs text-[#8A7F6C] flex items-center gap-1.5 mt-0.5">
            <span
              className="inline-block w-1.5 h-1.5 rounded-full"
              style={{ backgroundColor: isConnected ? "#6B7A5E" : "#B23A2E" }}
            />
            {isConnected ? "Connected" : "Reconnecting..."}
          </p>
        </div>
      </div>

      {error ? (
        <ServiceError err={error} />
      ) : (
        <>
          <div
            ref={messagesContainerRef}
            className="flex-1 overflow-y-auto p-2 mb-4"
          >
            {loading && (
              <PageLoader />
            )}
            {messages && messages.length > 0 ? (
              <>
                {messagesPage &&
                  messagesPage.pageable.pageNumber <
                    messagesPage.totalPages - 1 &&
                  !loading && (
                    <p
                      className="text-center text-xs text-[#B23A2E] hover:underline cursor-pointer mb-3"
                      onClick={handleLoadMoreMessages}
                    >
                      🚀 Load more messages
                    </p>
                  )}
                {messages.map((msg, idx) => {
                  const isOwn = msg.from === senderId;
                  const isGroupedWithPrev = idx > 0 && messages[idx - 1].from === msg.from;
                  const isLastInRun = idx === messages.length - 1 || messages[idx + 1].from !== msg.from;

                  const bubble = (
                    <div
                      className={`max-w-[70%] sm:max-w-[50%] px-4 py-2 shadow-sm border transition-shadow
                        ${isOwn
                          ? "ml-auto text-right bg-[#DDD0B0] border-[#C9A063] rounded-2xl rounded-br-sm"
                          : "bg-[#F3EBD9] border-[#C9A063] rounded-2xl rounded-bl-sm"
                        }
                        ${updateId === msg.id ? "ring-2 ring-[#D9A441]" : ""}
                      `}
                    >
                      <p className="text-[#241F1A] text-sm">{msg.content}</p>
                      {isLastInRun && (
                        <span className="text-[10px] text-[#8A7F6C] block mt-1">
                          {new Date(msg.createdAt).toLocaleTimeString([], {
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </span>
                      )}
                    </div>
                  );

                  return (
                    <div key={msg.id || idx} className={isGroupedWithPrev ? "mt-1" : "mt-4"}>
                      {isOwn ? (
                        <ContextMenu>
                          <ContextMenuTrigger asChild>
                            {bubble}
                          </ContextMenuTrigger>
                          <ContextMenuContent className="bg-[#F3EBD9] border-[#C9A063] rounded-sm">
                            <ContextMenuItem
                              className="text-[#241F1A] focus:bg-[#DDD0B0] focus:text-[#241F1A]"
                              onClick={() => {
                                setUpdateId(msg.id);
                                setMessage(msg.content);
                              }}
                            >
                              Edit Message
                            </ContextMenuItem>
                            <ContextMenuItem
                              className="text-[#B23A2E] focus:bg-[#B23A2E] focus:text-[#F3EBD9]"
                              onClick={() => {
                                deleteSpecificMessageById(msg.id)
                                  .then((response) => {
                                    if (response.status == 204) {
                                      const lastMsg = messages[messages.length - 1] == msg
                                      setMessages((prev) =>
                                        prev.filter((m) => m.id != msg.id)
                                      );
                                      const foundChat = allChats.find(obj => obj.id == msg.chatId);
                                      if (foundChat && lastMsg) {
                                        const newChat = {...foundChat, lastMessage: "*The message was deleted*"};
                                        setAllChats((prev) => [newChat, ...(prev.filter(chat => chat != foundChat))])
                                      }
                                    } else {
                                      toast.warning(
                                        "Unexpected response from server received: " +
                                          response.data
                                      );
                                    }
                                  })
                                  .catch((err) =>
                                    toast.error((err as AxiosError).message)
                                  );
                              }}
                            >
                              Delete Message
                            </ContextMenuItem>
                          </ContextMenuContent>
                        </ContextMenu>
                      ) : (
                        bubble
                      )}
                    </div>
                  );
                })}
                <div ref={messagesEndRef} />
              </>
            ) : !loading && (
              <div className="flex flex-col items-center justify-center py-12">
                <p className="font-hand text-xl text-[#8A7F6C]">no messages yet</p>
                <p className="text-sm text-[#8A7F6C] mt-1">start the conversation</p>
              </div>
            )}
          </div>

          {updateId != null && (
            <div className="flex items-center justify-between px-3 py-1.5 mb-2 bg-[#DDD0B0] border border-[#C9A063] rounded-sm text-xs text-[#4A4136]">
              <span>Editing message</span>
              <button
                onClick={() => {
                  setUpdateId(null);
                  setMessage("");
                }}
                className="text-[#8A7F6C] hover:text-[#B23A2E]"
                aria-label="Cancel edit"
              >
                <X size={14} />
              </button>
            </div>
          )}

          <form
            onSubmit={(e) => {
              e.preventDefault();
              if (updateId != null) {
                handleUpdateMessage();
              } else {
                handleSendMessage();
              }
            }}
            className="flex items-center gap-2 border-t border-[#C9A063] pt-4"
          >
            <input
              type="text"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Type a message..."
              className="flex-1 p-2.5 rounded-full bg-[#F3EBD9] border border-[#C9A063] text-[#241F1A] placeholder:text-[#8A7F6C] focus:outline-none focus:ring-2 focus:ring-[#D9A441]"
              disabled={!isConnected}
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  e.preventDefault();
                  if (updateId != null) {
                    handleUpdateMessage();
                  } else {
                    handleSendMessage();
                  }
                }
              }}
            />
            <button
              type="submit"
              disabled={!message.trim() || !isConnected}
              className="bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9] p-2.5 rounded-full disabled:bg-[#C9A063] disabled:cursor-not-allowed transition-colors"
              aria-label="Send message"
            >
              <Send size={18} />
            </button>
          </form>
        </>
      )}
    </div>
  );
};