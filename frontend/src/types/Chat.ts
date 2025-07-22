export type ChatListResponse = {
  id: string;
  participants: ChatUser[];
};

export type ChatUser = {
  id: string,
  name: string,
  username: string
};

export type Message = {
  id: string,
  chatId: string,
  to: string,
  from: string,
  content: string,
  createdAt: string
}