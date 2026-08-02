package com.olelllka.chat_service.configuration;

import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.ProfileDocumentRepository;
import com.olelllka.chat_service.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatRepository chatRepository;
    private final MessagePublisher publisher;
    private final ProfileDocumentRepository documentRepository;
    private final JWTUtil jwtUtil;
    private final MessageService messageService;
    private final AsyncMessageHandlingService messageHandlingService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // url will be: ws://localhost:8080/chat?from={id}&to={id}
        registry.addHandler(new ChatWebSocketHandler(chatRepository, publisher, messageHandlingService, documentRepository, jwtUtil), "/chat")
                .setAllowedOrigins("*");
    }

}