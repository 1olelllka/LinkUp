package com.olelllka.chat_service.service;

import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {
    Page<ChatEntity> getChatsForUser(String userId, Pageable pageable);

    ChatEntity createNewChat(String userId1, String userId2);

    void deleteChat(String chatId);

    Page<MessageEntity> getMessagesForChat(String chatId, Pageable pageable);

    MessageEntity updateMessage(String msgId, String msg_id, MessageDto updatedMsg);
}
