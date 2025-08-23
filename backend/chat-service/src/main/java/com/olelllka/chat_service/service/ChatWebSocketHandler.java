package com.olelllka.chat_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.chat_service.domain.dto.JWTMessage;
import com.olelllka.chat_service.domain.dto.NotificationDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.domain.entity.User;
import com.olelllka.chat_service.feign.ProfileFeign;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private ChatRepository chatRepository;
    private MessageRepository messageRepository;
    private MessagePublisher messagePublisher;
    private JWTUtil jwtUtil;
    private ProfileFeign profileService;
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UUID> authenticatedSessions = new ConcurrentHashMap<>();
    private ResponseEntity<User> req1;
    private ResponseEntity<User> req2;

    @Autowired
    public ChatWebSocketHandler(ChatRepository chatRepository,
                                MessageRepository messageRepository,
                                ProfileFeign profileService,
                                MessagePublisher messagePublisher,
                                JWTUtil jwtUtil) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.messagePublisher = messagePublisher;
        this.profileService = profileService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sender = session.getUri().getQuery().split("=")[1].substring(0, 36);
        String receiver = session.getUri().getQuery().split("=")[2];
        this.req1 = profileService.getProfileById(UUID.fromString(sender));
        this.req2 = profileService.getProfileById(UUID.fromString(receiver));
        if (this.req1.getStatusCode().is4xxClientError()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A client error occurred on external service."));
        } else if (this.req1.getStatusCode().is5xxServerError()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A server error occurred on external service."));
        }
        if (this.req2.getStatusCode().is4xxClientError()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A client error occurred on external service."));
        } else if (this.req2.getStatusCode().is5xxServerError()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A server error occurred on external service."));
        }
        sessions.put(sender + ":" + receiver, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = message.getPayload();
        UUID senderId = UUID.fromString(session.getUri().getQuery().split("=")[1].substring(0, 36));
        if (!authenticatedSessions.containsKey(session.getId())) {
            JWTMessage msg = objectMapper.readValue(payload, JWTMessage.class);

            if (!jwtUtil.isTokenValid(msg.getToken())) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Invalid or expired token."));
            }

            if (!jwtUtil.extractId(msg.getToken()).equals(senderId.toString())) {
                session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Bad authorization."));
            }
            authenticatedSessions.put(session.getId(), senderId);
            return;
        }
        MessageEntity msg = objectMapper.readValue(payload, MessageEntity.class);
        UUID targetUserId = UUID.fromString(session.getUri().getQuery().split("=")[2]);
        String chatMessage = msg.getContent();

        WebSocketSession targetSession = sessions.get(targetUserId + ":" + senderId);
        Optional<ChatEntity> chat = chatRepository.findChatByTwoMembers(senderId, targetUserId);
        String chatId;
        if (chat.isEmpty()) {
            User users[] = {this.req1.getBody(), this.req2.getBody()};
            ChatEntity newChat = chatRepository.save(ChatEntity.builder().participants(users).build());
            chatId = newChat.getId();
        } else {
            chatId = chat.get().getId();
        }
        if (targetSession != null && targetSession.isOpen()) {
//            targetSession.sendMessage(new TextMessage(senderId + ": " + chatMessage));
            targetSession.sendMessage(new TextMessage(chatMessage));

        } else {
            // DEBUG
//            session.sendMessage(new TextMessage("User " + targetUserId + " is not available."));
            NotificationDto notification = NotificationDto.builder()
                    .read(false)
                    .createdAt(new Date())
                    .userId(targetUserId.toString())
                    .text("User @" + req2.getBody().getUsername() + " sent you a message: " + chatMessage)
                    .build();
            messagePublisher.createChatNotification(notification);
        }
        // I've implemented sync write into db for simplicity, in future I'll try to do this async to prevent db overhead.
        messageRepository.save(MessageEntity
                .builder()
                .id(msg.getId())
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
