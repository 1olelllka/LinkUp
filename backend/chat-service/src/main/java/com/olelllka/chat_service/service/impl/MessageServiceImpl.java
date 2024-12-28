package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.repository.MessageRepository;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository repository;

    @Override
    public Page<MessageEntity> getMessagesForChat(String chatId, Pageable pageable) {
        return repository.findByChatId(chatId, pageable);
    }

    @Override
    public MessageEntity updateMessage(String msgId, MessageDto updatedMsg) {
        return repository.findById(msgId).map(msg -> {
            Optional.of(updatedMsg.getContent()).ifPresent(msg::setContent);
            return repository.save(msg);
        }).orElseThrow(() -> new NotFoundException("Message with such id was not found."));
    }

    @Override
    public void deleteSpecificMessage(String msgId) {
        repository.deleteById(msgId);
    }
}
