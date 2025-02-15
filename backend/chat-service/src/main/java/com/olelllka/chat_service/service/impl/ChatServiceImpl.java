package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.feign.ProfileFeign;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.ChatService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
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
@Log
public class ChatServiceImpl implements ChatService {

    private final ChatRepository repository;
    private final MongoTemplate mongoTemplate;
    private final ProfileFeign profileService;

    @Override
    @CircuitBreaker(name = "chat-service", fallbackMethod = "pageOfChatsFallback")
    public Page<ChatEntity> getChatsForUser(UUID userId, Pageable pageable) {
        if (!profileService.getProfileById(userId).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("User with such id does not exist.");
        }
        return repository.findChatsByUserId(userId, pageable);
    }

    @Override
    public ChatEntity createNewChat(@NotEmpty UUID userId1, @NotEmpty UUID userId2) {
        if (!profileService.getProfileById(userId1).getStatusCode().is2xxSuccessful() ||
        !profileService.getProfileById(userId2).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("One of the users with such id does not exist.");
        }
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

    private Page<ChatEntity> pageOfChatsFallback(UUID userId, Pageable pageable, Throwable t) {
        if (t instanceof NotFoundException) {
            throw new NotFoundException(t.getMessage());
        }
        log.warning("Circuit Breaker Triggered: " + t.getMessage());
        return Page.empty();
    }
}
