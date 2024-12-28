package com.olelllka.chat_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private ObjectMapper objectMapper;
    private ChatRepository chatRepository;
    private MessageRepository messageRepository;
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public ChatWebSocketHandler(ChatRepository chatRepository,
                                MessageRepository messageRepository) {
        this.objectMapper = new ObjectMapper();
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = session.getUri().getQuery().split("=")[1];
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("Connection established! Your userId: " + userId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        MessageEntity msg = objectMapper.readValue(payload, MessageEntity.class);
        String senderId = session.getUri().getQuery().split("=")[1];
        String targetUserId = msg.getTo();
        String chatMessage = msg.getContent();

        WebSocketSession targetSession = sessions.get(targetUserId);
        Optional<ChatEntity> chat = chatRepository.findChatByTwoMembers(session.getUri().getQuery().split("=")[1], targetUserId);
        String chatId;
        if (chat.isEmpty()) {
            String users[] = {senderId, targetUserId};
            ChatEntity newChat = chatRepository.save(ChatEntity.builder().participants(users).build());
            chatId = newChat.getId();
        } else {
            chatId = chat.get().getId();
        }
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(senderId + ": " + chatMessage));
        } else {
            session.sendMessage(new TextMessage("User " + targetUserId + " is not available."));
        }
        // I've implemented sync write into db for simplicity, in future I'll try to do this async to prevent db overhead.
        messageRepository.save(MessageEntity
                .builder()
                .chatId(chatId)
                .from(senderId)
                .to(targetUserId)
                .createdAt(new Date())
                .content(chatMessage)
                .build());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().remove(session);
    }

}
