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

type ChatWindowProps = {
  chatId: string;
  senderId: string | undefined;
  receiverId: string | undefined;
  receiverName: string;
  setRefresh: (page: number) => void;
  allChats: ChatListResponse[]
};

export const ChatWindow = ({
  chatId,
  senderId,
  receiverName,
  receiverId,
  setRefresh,
  allChats
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
      if (!allChats.find((chat) => chat.id == chatId)) {
        // trigger update list of chats if such chat does not exist
        // if chat exists on another page which wasn't fetched yet, it'll
        // be moved to the top of list by backend
        setRefresh(Math.random() * 1000);
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

  return (
    <div className="flex flex-col h-full max-h-[90vh]">
      <div className="mb-4">
        <h2 className="text-2xl font-bold">Chat with {receiverName}</h2>
        <p className="text-sm text-gray-500">
          Status:{" "}
          <span
            className={
              connectionStatus === "Open" ? "text-green-500" : "text-red-500"
            }
          >
            {connectionStatus}
          </span>
        </p>
      </div>

      {error ? (
        <ServiceError err={error} />
      ) : (
        <>
          <div
            ref={messagesContainerRef}
            className="flex-1 space-y-4 overflow-y-auto p-2 mb-4"
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
                      className="text-center text-xs text-slate-500 hover:underline cursor-pointer"
                      onClick={handleLoadMoreMessages}
                    >
                      🚀 Load more messages
                    </p>
                  )}
                {messages.map((msg, idx) =>
                  msg.from === senderId ? (
                    <ContextMenu>
                      <ContextMenuTrigger asChild>
                        <div
                          key={msg.id || idx}
                          className={`max-w-[50%] px-4 py-2 rounded-xl shadow-sm bg-blue-100 ml-auto text-right
                        ${updateId === msg.id && "shadow-xl"}
                        `}
                        >
                          <p>{msg.content}</p>
                          <span className="text-xs text-gray-500 block mt-1">
                            {new Date(msg.createdAt).toLocaleTimeString([], {
                              hour: "2-digit",
                              minute: "2-digit",
                            })}
                          </span>
                        </div>
                      </ContextMenuTrigger>
                      <ContextMenuContent>
                        <ContextMenuItem
                          onClick={() => {
                            setUpdateId(msg.id);
                            setMessage(msg.content);
                          }}
                        >
                          Edit Message
                        </ContextMenuItem>
                        <ContextMenuItem
                          onClick={() => {
                            deleteSpecificMessageById(msg.id)
                              .then((response) => {
                                if (response.status == 204) {
                                  setMessages((prev) =>
                                    prev.filter((m) => m.id != msg.id)
                                  );
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
                    <div
                      key={msg.id || idx}
                      className="max-w-[50%] px-4 py-2 rounded-xl shadow-sm bg-gray-100 text-left"
                    >
                      <p>{msg.content}</p>
                      <span className="text-xs text-gray-500 block mt-1">
                        {new Date(msg.createdAt).toLocaleTimeString([], {
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </span>
                    </div>
                  )
                )}
                <div ref={messagesEndRef} />
              </>
            ) : !loading && (
              <div className="text-center text-gray-500 py-8">
                🍪 No messages yet. Start the conversation!
              </div>
            )}
          </div>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              if (updateId != null) {
                handleUpdateMessage();
              } else {
                handleSendMessage();
              }
            }}
            className="flex items-center gap-2 border-t pt-4"
          >
            <input
              type="text"
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              placeholder="Type a message..."
              className="flex-1 p-2.5 rounded-full bg-white border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-200 mb-1"
              disabled={connectionStatus !== "Open"}
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
            {updateId != null && (
              <button
                className="text-white px-4 py-2 rounded-full bg-red-500"
                onClick={() => {
                  setUpdateId(null);
                  setMessage("");
                }}
              >
                Cancel
              </button>
            )}
            <button
              type="submit"
              disabled={!message.trim() || connectionStatus !== "Open"}
              className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-full disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              Send
            </button>
          </form>
        </>
      )}
    </div>
  );
};
