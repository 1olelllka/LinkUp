import { useMessageList } from "@/hooks/useMessageList";

type ChatWindowProps = {
  chatId: string;
  senderId: string;
  senderName: string;
};

export const ChatWindow = ({ chatId, senderId, senderName }: ChatWindowProps) => {
  const messages = useMessageList(chatId);
  console.log(messages)
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
