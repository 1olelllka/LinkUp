package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.domain.entity.ProfileDocument;
import com.olelllka.chat_service.domain.entity.User;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.ProfileDocumentRepository;
import com.olelllka.chat_service.rest.exception.AuthException;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.ChatService;
import com.olelllka.chat_service.service.JWTUtil;
import io.jsonwebtoken.JwtException;
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
    private final JWTUtil jwtUtil;
    private final ProfileDocumentRepository documentRepository;

    @Override
    public Page<ChatEntity> getChatsForUser(UUID userId, Pageable pageable, String jwt) {
        try {
            if (!jwtUtil.extractId(jwt).equals(userId.toString())) {
                throw new AuthException("You're unauthorized to perform such operation.");
            }
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthException(ex.getMessage());
        }
        return repository.findChatsByUserId(userId, pageable);
    }

    // This is being used only in tests
    @Override
    public ChatEntity createNewChat(@NotEmpty UUID userId1, @NotEmpty UUID userId2) {
        ProfileDocument doc1 = documentRepository.findById(userId1).orElseThrow(() -> new NotFoundException("User with id %s was not found".formatted(userId1.toString())));
        User user1 = User.builder().id(doc1.getId()).name(doc1.getName()).username(doc1.getUsername()).photo(doc1.getPhoto()).build();
        ProfileDocument doc2 = documentRepository.findById(userId2).orElseThrow(() -> new NotFoundException("User with id %s was not found".formatted(userId2.toString())));
        User user2 = User.builder().id(doc2.getId()).name(doc2.getName()).username(doc2.getUsername()).photo(doc2.getPhoto()).build();
        ChatEntity newChat = ChatEntity.builder()
                .participants(new User[]{user1, user2})
                .build();
        return repository.save(newChat);
    }

    @Override
    @Transactional
    public void deleteChat(String chatId, String jwt) {
        if (repository.existsById(chatId)) {
            ChatEntity entity = repository.findById(chatId).get();
            try {
                if (!jwtUtil.extractId(jwt).equals(entity.getParticipants()[0].getId().toString()) && !jwtUtil.extractId(jwt).equals(entity.getParticipants()[1].getId().toString())) {
                    throw new AuthException("You're unauthorized to perform such operation.");
                }
            } catch (JwtException | IllegalArgumentException ex) {
                throw new AuthException(ex.getMessage());
            }
        }
        repository.deleteById(chatId);
        Query query = new Query();
        query.addCriteria(Criteria.where("chatId").is(chatId));
        mongoTemplate.findAllAndRemove(query, MessageEntity.class, "Message");
    }

    @Override
    public ChatEntity getChatByTwoUsers(UUID id1, UUID id2, String token) {
        try {
            if (!jwtUtil.isTokenValid(token) || (!jwtUtil.extractId(token).equals(id1.toString()) && jwtUtil.extractId(token).equals(id2.toString()))) {
                throw new AuthException("You are not authorized to perform such operation.");
            }
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthException(ex.getMessage());
        }
        return repository.findChatByTwoMembers(id1, id2)
                .orElseThrow(() -> new NotFoundException("Chat with such users was not found."));
    }
}
