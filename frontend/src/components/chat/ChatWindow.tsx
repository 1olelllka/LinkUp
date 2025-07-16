import axios from "axios";
import { useEffect, useState } from "react";

type ChatWindowProps = {
  chatId: string;
  senderId: string;
  senderName: string;
};

type Message = {
  id: string,
  chatId: string,
  to: string,
  from: string,
  content: string,
  createdAt: string
}

export const ChatWindow = ({ chatId, senderId, senderName }: ChatWindowProps) => {
  const [messages, setMessages] = useState<Message[]>();

  useEffect(() => {
    axios.get(`http://localhost:8080/api/chats/${chatId}/messages`, {
      headers: {
        Authorization: "Bearer eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJMaW5rVXAiLCJzdWIiOiI0MzZjNWE3OS1lZTM1LTQ5OTUtODZkMS00NzVlM2ExNGQ1ODQiLCJpYXQiOjE3NTI2MDgwMTAsImV4cCI6MTc1MjYxMTYxMH0.1Rz9zp0tkUmFyYscYAM-olWdhhkRZboqUaxPkxAnkiuyY18FHnIyG2qbQ-EP_8qV"
      }
    }).then(response => {
      setMessages(response.data.content);
    }).catch(err => console.log(err));
  }, [chatId])

  return (
    <div className="flex flex-col h-full max-h-[90vh]">
      {/* Chat header */}
      <div className="mb-4">
        <h2 className="text-2xl font-bold">Chat with {senderName}</h2>
      </div>

      {/* Messages */}
      <div className="flex-1 space-y-4 overflow-y-auto p-2 mb-4">
        {messages?.map((msg, idx) => (
          <div
            key={idx}
            className={`max-w-[75%] px-4 py-2 rounded-xl shadow-sm ${
              msg.from == senderId
                ? "bg-blue-100 ml-auto text-right"
                : "bg-gray-100 text-left"
            }`}
          >
            <p>{msg.content}</p>
            <span className="text-xs text-gray-500 block mt-1">{new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
          </div>
        ))}
      </div>

      {/* Message input */}
      <form className="flex items-center gap-2 border-t pt-4">
        <input
          type="text"
          placeholder="Type a message..."
          className="flex-1 p-3 rounded-full bg-white border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-200"
        />
        <button
          type="submit"
          className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-full"
        >
          Send
        </button>
      </form>
    </div>
  );
};
