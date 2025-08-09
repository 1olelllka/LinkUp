import { fetchMessagesList } from "@/services/chatServices";
import type { Message, MessagePage } from "@/types/Chat";
import { useCallback, useEffect, useState } from "react";

export const useMessageList = (chatId: string) => {
  const [messagesPage, setMessagesPage] = useState<MessagePage>();
  const [messages, setMessages] = useState<Message[]>([]);
  const [page, setPage] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    if (chatId) {
      setLoading(true);
      fetchMessagesList(chatId, 0)
        .then((page: MessagePage) => {
          setMessagesPage(page);
          setMessages(page.content.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()));
          setPage(0);
        })
        .catch(err => console.log(err))
        .finally(() => setLoading(false));
    }
  }, [chatId]);

  const loadMoreMessages = useCallback(async (pageNumber: number) => {
    if (loading || !chatId) return;
    setLoading(true);
    try {
      const res = await fetchMessagesList(chatId, pageNumber);
      setMessagesPage(res)
      const sortedNewMessages = res.content.sort(
        (a : Message, b: Message) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
      setMessages((prev) => [...sortedNewMessages, ...prev]);
      setPage(pageNumber);
      console.log('success');
    } catch (err) {
      console.log(err);
    } finally {
      setLoading(false);
    }
  }, [chatId, loading]);



  return { messagesPage, messages, setMessages, loading, loadMoreMessages, page };
};