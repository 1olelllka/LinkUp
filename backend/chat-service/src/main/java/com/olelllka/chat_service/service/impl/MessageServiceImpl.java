package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.AuthException;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.JWTUtil;
import com.olelllka.chat_service.service.MessageService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log
public class MessageServiceImpl implements MessageService {

    private final MessageRepository repository;
    private final ChatRepository chatRepository;
    private final JWTUtil jwtUtil;

    @Override
    public Page<MessageEntity> getMessagesForChat(String chatId, Pageable pageable, String jwt) {
        Page<MessageEntity> messages = repository.findByChatId(chatId, pageable);
        if (messages.hasContent()) {
            MessageEntity entity = messages.getContent().getFirst();
            try {
                if (!jwtUtil.extractId(jwt).equals(entity.getFrom().toString()) && !jwtUtil.extractId(jwt).equals(entity.getTo().toString())) {
                    throw new AuthException("You're unauthorized to perform such operation.");
                }
            } catch (JwtException | IllegalArgumentException ex) {
                throw new AuthException(ex.getMessage());
            }
        }
        return messages;
    }

    @Override
    public MessageEntity updateMessage(String msgId, MessageDto updatedMsg, String jwt) {
        return repository.findById(msgId).map(msg -> {
            try {
                if (!jwtUtil.extractId(jwt).equals(msg.getFrom().toString())) {
                    throw new AuthException("You're unauthorized to perform such operation.");
                }
            } catch (JwtException | IllegalArgumentException ex) {
                throw new AuthException(ex.getMessage());
            }
            Optional.of(updatedMsg.getContent()).ifPresent(msg::setContent);
            return repository.save(msg);
        }).orElseThrow(() -> new NotFoundException("Message with such id was not found."));
    }

    @Override
    @Async
    public void saveMessageToDatabase(MessageEntity entity) {
        repository.save(entity);
        chatRepository.findById(entity.getChatId()).map((chat) -> {
            chat.setLastMessage(entity.getContent());
            chat.setTime(entity.getCreatedAt());
            chatRepository.save(chat);
            return chat;
        }).orElseThrow(() -> new NotFoundException("Chat with such id does not exist."));
    }

    @Override
    public void deleteSpecificMessage(String msgId, String jwt) {
        if (repository.existsById(msgId)) {
            MessageEntity entity = repository.findById(msgId).get();
            try {
                if (!jwtUtil.extractId(jwt).equals(entity.getFrom().toString())) {
                    throw new AuthException("You're unauthorized to perform such operation.");
                }
            } catch (JwtException | IllegalArgumentException ex) {
                throw new AuthException(ex.getMessage());
            }
        }
        repository.deleteById(msgId);
    }
}
