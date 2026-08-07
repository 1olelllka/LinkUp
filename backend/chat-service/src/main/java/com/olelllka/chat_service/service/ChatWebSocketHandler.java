package com.olelllka.chat_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.chat_service.domain.dto.JWTMessage;
import com.olelllka.chat_service.domain.dto.NotificationDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.domain.entity.ProfileDocument;
import com.olelllka.chat_service.domain.entity.User;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.ProfileDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private ChatRepository chatRepository;
    private MessagePublisher messagePublisher;
    private JWTUtil jwtUtil;
    private ProfileDocumentRepository documentRepository;
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, UUID> authenticatedSessions = new ConcurrentHashMap<>();
    private User user1;
    private User user2;
    private AsyncMessageHandlingService messageHandlingService;

    @Autowired
    public ChatWebSocketHandler(ChatRepository chatRepository,
                                MessagePublisher messagePublisher,
                                AsyncMessageHandlingService messageHandlingService,
                                ProfileDocumentRepository documentRepository,
                                JWTUtil jwtUtil) {
        this.chatRepository = chatRepository;
        this.messagePublisher = messagePublisher;
        this.jwtUtil = jwtUtil;
        this.messageHandlingService = messageHandlingService;
        this.documentRepository = documentRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sender = session.getUri().getQuery().split("=")[1].substring(0, 36);
        String receiver = session.getUri().getQuery().split("=")[2];
        Optional<ProfileDocument> doc1 = documentRepository.findById(UUID.fromString(sender));
        Optional<ProfileDocument> doc2 = documentRepository.findById(UUID.fromString(receiver));
        if (doc1.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A sender was not found."));
        }
        if (doc2.isEmpty()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A receiver was not found."));
        }
        this.user1 = User.builder().id(doc1.get().getId()).username(doc1.get().getUsername()).name(doc1.get().getName()).build();
        this.user2 = User.builder().id(doc2.get().getId()).username(doc2.get().getUsername()).name(doc2.get().getName()).build();
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
            User users[] = {this.user1, this.user2};
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
                    .text("User @" + user1.getUsername() + " sent you a message: " + chatMessage)
                    .build();
            messagePublisher.createChatNotification(notification);
        }
        MessageEntity msgToSave = MessageEntity
                .builder()
                .id(msg.getId())
                .chatId(chatId)
                .from(senderId)
                .to(targetUserId)
                .createdAt(new Date())
                .content(chatMessage)
                .build();
        messageHandlingService.saveMessageToDatabase(msgToSave);
//        messageService.saveMessageToDatabase(msgToSave);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.values().remove(session);
    }

}
