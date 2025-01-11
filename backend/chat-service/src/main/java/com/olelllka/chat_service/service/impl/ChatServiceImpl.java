package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.service.ChatService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository repository;
    private final MongoTemplate mongoTemplate;

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
    @Transactional
    public void deleteChat(String chatId) {
        repository.deleteById(chatId);
        Query query = new Query();
        query.addCriteria(Criteria.where("chatId").is(chatId));
        mongoTemplate.findAllAndRemove(query, MessageEntity.class, "Message");
    }
}
