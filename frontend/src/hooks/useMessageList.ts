import { fetchMessagesList } from "@/services/chatServices";
import type { Message } from "@/types/Chat";
import { useEffect, useState } from "react";


export const useMessageList = (chatId : string) => {
        const [messages, setMessages] = useState<Message[]>();

        useEffect(() => {
            fetchMessagesList(chatId)
            .then(setMessages)
            .catch(err => console.log(err));
        }, [chatId])

        return messages;
}