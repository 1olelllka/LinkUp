package com.olelllka.chat_service.configuration;

import com.olelllka.chat_service.feign.ProfileFeign;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.service.ChatWebSocketHandler;
import com.olelllka.chat_service.service.JWTUtil;
import com.olelllka.chat_service.service.MessagePublisher;
import com.olelllka.chat_service.service.MessageService;
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
    private final ProfileFeign profileService;
    private final JWTUtil jwtUtil;
    private final MessageService messageService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // url will be: ws://localhost:8080/chat?from={id}&to={id}
        registry.addHandler(new ChatWebSocketHandler(chatRepository, profileService, publisher, messageService, jwtUtil), "/chat")
                .setAllowedOrigins("*");
    }

}