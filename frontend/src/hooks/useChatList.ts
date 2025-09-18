import { fetchChatList } from "@/services/chatServices";
import type { ChatListResponse, ChatPage } from "@/types/Chat";
import { AxiosError } from "axios";
import { useEffect, useState } from "react";

export const useChatList = (userId: string | undefined, initialPage: number = 0) => {
  const [pageNumber, setPageNumber] = useState(initialPage);
  const [chatUsersPage, setChatUsersPage] = useState<ChatPage | null>(null);
  const [allChats, setAllChats] = useState<ChatListResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<AxiosError>();
  const [refresh, setRefresh] = useState(0);
  
  
  useEffect(() => {
    if (!userId) return;
    console.log('triggered');
    setLoading(true);
    // setTimeout(() => {
      fetchChatList(userId, pageNumber)
        .then((response) => {
          if (pageNumber === 0) {
            setAllChats(response.content);
          } else {
            setAllChats((prev) => [...prev, ...response.content]);
          }
          setChatUsersPage(response);
        })
        .catch((err) => setError(err as AxiosError))
        .finally(() => setLoading(false));
    // }, 2500);
  }, [userId, pageNumber, refresh]);

  const loadNextPage = () => {
    if (chatUsersPage && !chatUsersPage.last && !loading) {
      setPageNumber((prev) => prev + 1);
    }
  };

  return { allChats, setAllChats, chatUsersPage, loadNextPage, loading, setPageNumber, error, setRefresh };
};
