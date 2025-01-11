package com.olelllka.chat_service.configuration;

import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.service.ChatWebSocketHandler;
import com.olelllka.chat_service.service.MessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MessagePublisher publisher;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // url will be: ws://localhost:8080/chat?userId={id}
        registry.addHandler(new ChatWebSocketHandler(chatRepository, messageRepository, publisher), "/chat")
                .setAllowedOrigins("*");
    }

}
