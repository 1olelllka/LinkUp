package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.domain.entity.User;
import com.olelllka.chat_service.feign.ProfileFeign;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.rest.exception.AuthException;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.ChatService;
import com.olelllka.chat_service.service.JWTUtil;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository repository;
    private final MongoTemplate mongoTemplate;
    private final ProfileFeign profileService;
    private final JWTUtil jwtUtil;

    @Override
    public Page<ChatEntity> getChatsForUser(UUID userId, Pageable pageable, String jwt) {
        if (!jwtUtil.extractId(jwt).equals(userId.toString())) {
            throw new AuthException("You're unauthorized to perform such operation.");
        }
        return repository.findChatsByUserId(userId, pageable);
    }

    @Override
    public ChatEntity createNewChat(@NotEmpty UUID userId1, @NotEmpty UUID userId2) {
//        if (!jwtUtil.extractId(jwt).equals(userId1.toString()) && !jwtUtil.extractId(jwt).equals(userId2.toString())) {
//            throw new AuthException("You're unauthorized to perform such operation");
//        }
        ResponseEntity<User> req1 = profileService.getProfileById(userId1);
        ResponseEntity<User> req2 = profileService.getProfileById(userId2);
        if (!req1.getStatusCode().is2xxSuccessful() ||
        !req2.getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("One of the users with such id does not exist.");
        }
        ChatEntity newChat = ChatEntity.builder()
                .participants(new User[]{req1.getBody(), req2.getBody()})
                .build();
        return repository.save(newChat);
    }

    @Override
    @Transactional
    public void deleteChat(String chatId, String jwt) {
        if (repository.existsById(chatId)) {
            ChatEntity entity = repository.findById(chatId).get();
            if (!jwtUtil.extractId(jwt).equals(entity.getParticipants()[0].getId().toString()) && !jwtUtil.extractId(jwt).equals(entity.getParticipants()[1].getId().toString())) {
                throw new AuthException("You're unauthorized to perform such operation.");
            }
        }
        repository.deleteById(chatId);
        Query query = new Query();
        query.addCriteria(Criteria.where("chatId").is(chatId));
        mongoTemplate.findAllAndRemove(query, MessageEntity.class, "Message");
    }

    @Override
    public ChatEntity getChatByTwoUsers(UUID id1, UUID id2, String token) {
        if (!jwtUtil.isTokenValid(token) || (!jwtUtil.extractId(token).equals(id1.toString()) && jwtUtil.extractId(token).equals(id2.toString()))) {
            throw new AuthException("You are not authorized to perform such operation.");
        }
        return repository.findChatByTwoMembers(id1, id2)
                .orElseThrow(() -> new NotFoundException("Chat with such users was not found."));
    }
}
