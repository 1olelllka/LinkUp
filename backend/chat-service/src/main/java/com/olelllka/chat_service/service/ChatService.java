package com.olelllka.chat_service.service;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ChatService {
    Page<ChatEntity> getChatsForUser(UUID userId, Pageable pageable);

    ChatEntity createNewChat(@NotEmpty UUID userId1, @NotEmpty UUID userId2);

    void deleteChat(String chatId);
}
