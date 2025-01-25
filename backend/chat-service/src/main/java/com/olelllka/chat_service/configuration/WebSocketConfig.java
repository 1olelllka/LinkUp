package com.olelllka.chat_service.configuration;

import com.olelllka.chat_service.feign.ProfileFeign;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.service.ChatWebSocketHandler;
import com.olelllka.chat_service.service.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessagePublisher publisher;
    private final ProfileFeign profileService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // url will be: ws://localhost:8080/chat?userId={id}
        registry.addHandler(new ChatWebSocketHandler(chatRepository, messageRepository, profileService, publisher), "/chat")
                .setAllowedOrigins("*");
    }

}