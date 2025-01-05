package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.service.ChatService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository repository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Page<ChatEntity> getChatsForUser(UUID userId, Pageable pageable) {
        return repository.findChatsByUserId(userId, pageable);
    }

    @Override
    public ChatEntity createNewChat(@NotEmpty UUID userId1, @NotEmpty UUID userId2) {
        ChatEntity newChat = ChatEntity.builder()
                .participants(new UUID[]{userId1, userId2})
                .build();
        return repository.save(newChat);
    }

    @Override
    // TODO: delete messages too!!!!
    public void deleteChat(String chatId) {
        repository.deleteById(chatId);
    }
}
