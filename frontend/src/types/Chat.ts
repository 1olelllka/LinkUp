export type ChatListResponse = {
  id: string;
  participants: ChatUser[];
};

export type ChatUser = {
  id: string,
  name: string,
  username: string
};


export type ChatPage = {
  content: ChatListResponse[],
  pageable: {
    pageNumber: number
  },
  totalPages: number,
  totalElements: number,
  last: boolean
}

export type Message = {
  id: string,
  chatId: string,
  to: string,
  from: string,
  content: string,
  createdAt: string
}

export type MessagePage = {
  content: Message[],
  pageable: {
    pageNumber: number
  },
  totalPages: number,
  totalElements: number,
  last: boolean
}