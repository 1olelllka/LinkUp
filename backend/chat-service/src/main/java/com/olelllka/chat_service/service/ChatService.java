package com.olelllka.chat_service.service;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {
    Page<ChatEntity> getChatsForUser(String userId, Pageable pageable);

    ChatEntity createNewChat(String userId1, String userId2);
}
