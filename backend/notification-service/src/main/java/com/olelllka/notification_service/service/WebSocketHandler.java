package com.olelllka.notification_service.service;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = getUserIdFromUri(session);
        sessions.put(userId, session);
        session.sendMessage(new TextMessage("Connection established! Your userId: " + userId));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        UUID userId = getUserIdFromUri(session);
        if (userId != null) {
            sessions.remove(userId);
        }
    }

    public void sendNotificationToUser(UUID userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private UUID getUserIdFromUri(WebSocketSession session) {
        return UUID.fromString(session.getUri().getQuery().split("=")[1]);
    }
}
