import { fetchChatList } from "@/services/chatServices";
import type { ChatListResponse } from "@/types/Chat";
import { useEffect, useState } from "react";

export const useChatList = (userId: string | undefined) => {
    const [chatUsers, setChatUsers] = useState<ChatListResponse[]>();

    useEffect(() => {
        if (userId) {
            fetchChatList(userId).then((response) => {
                setChatUsers(response.content);
            }).catch(err => console.log(err));
        }
    }, [userId]);

    return chatUsers;
}