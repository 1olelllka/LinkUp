import { useEffect, useState, useCallback } from 'react';
import type { Message } from '@/types/Chat';

export const useChatWebSocket = (sender: string | undefined, receiver: string | undefined) => {
  const [socket, setSocket] = useState<WebSocket | null>(null);
  const [connectionStatus, setConnectionStatus] = useState<'Connecting' | 'Open' | 'Closing' | 'Closed'>('Closed');
  const [lastMessage, setLastMessage] = useState<Message | null>(null);

  useEffect(() => {
    if (!sender || !receiver) return;

    const ws = new WebSocket(`ws://localhost:8080/chat?from=${sender}&to=${receiver}`);
    setSocket(ws);
    setConnectionStatus('Connecting');

    ws.onopen = () => {
      console.log('WebSocket connected');
      setConnectionStatus('Open');
    };

    ws.onmessage = (event) => {
      console.log('Message received:', event.data);
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
      console.log('WebSocket disconnected');
      setConnectionStatus('Closed');
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    return () => {
      ws.close();
    };
  }, [sender, receiver]);

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
