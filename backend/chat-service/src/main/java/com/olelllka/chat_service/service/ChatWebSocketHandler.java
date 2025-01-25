package com.olelllka.chat_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.chat_service.domain.dto.NotificationDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.feign.ProfileFeign;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
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
    private MessageRepository messageRepository;
    private MessagePublisher messagePublisher;
    private ProfileFeign profileService;
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    public ChatWebSocketHandler(ChatRepository chatRepository,
                                MessageRepository messageRepository,
                                ProfileFeign profileService,
                                MessagePublisher messagePublisher) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.messagePublisher = messagePublisher;
        this.profileService = profileService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = session.getUri().getQuery().split("=")[1];
        if (!profileService.getProfileById(UUID.fromString(userId)).getStatusCode().is2xxSuccessful()){
            throw new NotFoundException("User with such id does not exist");
        }
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("Connection established! Your userId: " + userId));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = message.getPayload();
        MessageEntity msg = objectMapper.readValue(payload, MessageEntity.class);
        UUID senderId = UUID.fromString(session.getUri().getQuery().split("=")[1]);
        UUID targetUserId = msg.getTo();
        if (!profileService.getProfileById(targetUserId).getStatusCode().is2xxSuccessful()){
            throw new NotFoundException("User with such id does not exist");
        }
        String chatMessage = msg.getContent();

        WebSocketSession targetSession = sessions.get(targetUserId);
        Optional<ChatEntity> chat = chatRepository.findChatByTwoMembers(UUID.fromString(session.getUri().getQuery().split("=")[1]), targetUserId);
        String chatId;
        if (chat.isEmpty()) {
            UUID users[] = {senderId, targetUserId};
            ChatEntity newChat = chatRepository.save(ChatEntity.builder().participants(users).build());
            chatId = newChat.getId();
        } else {
            chatId = chat.get().getId();
        }
        if (targetSession != null && targetSession.isOpen()) {
            targetSession.sendMessage(new TextMessage(senderId + ": " + chatMessage));
        } else {
            session.sendMessage(new TextMessage("User " + targetUserId + " is not available."));
            NotificationDto notification = NotificationDto.builder()
                    .read(false)
                    .createdAt(new Date())
                    .userId(targetUserId.toString())
                    // TODO: When gateway implemented, put name instead of id!!!
                    .text("User with id: " + senderId + " sent you a message: " + chatMessage)
                    .build();
            messagePublisher.createChatNotification(notification);
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
