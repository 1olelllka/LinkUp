package com.olelllka.chat_service.configuration;

import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.service.ChatWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ChatRepository chatRepository;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { // url will be: ws://localhost:8080/chat?userId={id}
        registry.addHandler(new ChatWebSocketHandler(chatRepository, messageRepository), "/chat")
                .setAllowedOrigins("*");
    }

}
