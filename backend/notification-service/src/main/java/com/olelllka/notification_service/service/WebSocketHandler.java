package com.olelllka.notification_service.service;

import com.olelllka.notification_service.feign.ProfileFeign;
import com.olelllka.notification_service.rest.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final ConcurrentHashMap<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ProfileFeign profileService;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = getUserIdFromUri(session);
        if (profileService.getProfileById(userId).getStatusCode().is4xxClientError()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A client error occurred on external service."));
        } else if (profileService.getProfileById(userId).getStatusCode().is5xxServerError()) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("A server error occurred on external service."));
        }
        sessions.put(userId, session);
//        session.sendMessage(new TextMessage("Connection established! Your userId: " + userId));
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
