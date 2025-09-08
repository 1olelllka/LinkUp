package com.olelllka.chat_service.service;

import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

public interface MessageService {
    Page<MessageEntity> getMessagesForChat(String chatId, Pageable pageable, String jwt);

    MessageEntity updateMessage(String msgId, MessageDto updatedMsg, String jwt);

    void saveMessageToDatabase(MessageEntity entity);

    void deleteSpecificMessage(String msgId, String jwt);
}
