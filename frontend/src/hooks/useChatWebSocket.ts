import { useEffect, useState, useCallback } from 'react';
import type { Message } from '@/types/Chat';
import { useAuthStore } from '@/store/useAuthStore';

export const useChatWebSocket = (sender: string | undefined, receiver: string | undefined) => {
  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [connectionStatus, setConnectionStatus] = useState<'Connecting' | 'Open' | 'Closing' | 'Closed'>('Closed');
  const [lastMessage, setLastMessage] = useState<Message | null>(null);
  const token = useAuthStore.getState().token;

  useEffect(() => {
    if (!sender || !receiver) return;

    const ws = new WebSocket(`ws://localhost:8080/chat?from=${sender}&to=${receiver}`);
    setSocket(ws);
    setConnectionStatus('Connecting');

    ws.onopen = () => {
      ws.send(JSON.stringify({token: token}));
      setConnectionStatus('Open');
    };

    ws.onmessage = (event) => {
      try {
        const message: Message = JSON.parse(event.data);
        setLastMessage(message);
      } catch (error) {
        console.error('Error parsing message:', error);
        setLastMessage({
          id: Date.now().toString(),
          chatId: '',
          to: sender,
          from: receiver,
          content: event.data,
          createdAt: new Date().toISOString()
        });
      }
    };

    ws.onclose = () => {
      setConnectionStatus('Closed');
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    return () => {
      ws.close();
    };
  }, [sender, receiver, token]);

  const sendMessage = useCallback((message: string) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      socket.send(message);
      return true;
    } else {
      console.error('WebSocket is not open');
      return false;
    }
  }, [socket]);

  return {
    socket,
    connectionStatus,
    lastMessage,
    sendMessage
  };
};
