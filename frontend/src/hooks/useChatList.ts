import { fetchChatList } from "@/services/chatServices";
import type { ChatListResponse, ChatPage } from "@/types/Chat";
import { useEffect, useState } from "react";

export const useChatList = (userId: string | undefined, initialPage: number = 0) => {
  const [pageNumber, setPageNumber] = useState(initialPage);
  const [chatUsersPage, setChatUsersPage] = useState<ChatPage | null>(null);
  const [allChats, setAllChats] = useState<ChatListResponse[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!userId) return;
    if (pageNumber === 0) {
      setAllChats([]);
    }

    setLoading(true);
    fetchChatList(userId, pageNumber)
      .then((response) => {
        if (pageNumber === 0) {
          // Replace list on first page
          setAllChats(response.content);
        } else {
          // Append on subsequent pages
          setAllChats((prev) => [...prev, ...response.content]);
        }
        setChatUsersPage(response);
      })
      .catch((err) => console.log(err))
      .finally(() => setLoading(false));
  }, [userId, pageNumber]);

  const loadNextPage = () => {
    if (chatUsersPage && !chatUsersPage.last && !loading) {
      setPageNumber((prev) => prev + 1);
    }
  };

  return { allChats, setAllChats, chatUsersPage, loadNextPage, loading, setPageNumber };
};
