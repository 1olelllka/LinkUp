package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.AuthException;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.JWTUtil;
import com.olelllka.chat_service.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository repository;
    private final JWTUtil jwtUtil;

    @Override
    public Page<MessageEntity> getMessagesForChat(String chatId, Pageable pageable, String jwt) {
        Page<MessageEntity> messages = repository.findByChatId(chatId, pageable);
        if (messages.hasContent()) {
            MessageEntity entity = messages.getContent().getFirst();
            if (!jwtUtil.extractId(jwt).equals(entity.getFrom().toString()) && !jwtUtil.extractId(jwt).equals(entity.getTo().toString())) {
                throw new AuthException("You're unauthorized to perform such operation.");
            }
        }
        return messages;
    }

    @Override
    public MessageEntity updateMessage(String msgId, MessageDto updatedMsg, String jwt) {
        return repository.findById(msgId).map(msg -> {
            if (!jwtUtil.extractId(jwt).equals(msg.getFrom().toString())) {
                throw new AuthException("You're unauthorized to perform such operation.");
            }
            Optional.of(updatedMsg.getContent()).ifPresent(msg::setContent);
            return repository.save(msg);
        }).orElseThrow(() -> new NotFoundException("Message with such id was not found."));
    }

    @Override
    public void deleteSpecificMessage(String msgId, String jwt) {
        if (repository.existsById(msgId)) {
            MessageEntity entity = repository.findById(msgId).get();
            if (!jwtUtil.extractId(jwt).equals(entity.getFrom().toString())) {
                throw new AuthException("You're unauthorized to perform such operation.");
            }
        }
        repository.deleteById(msgId);
    }
}
