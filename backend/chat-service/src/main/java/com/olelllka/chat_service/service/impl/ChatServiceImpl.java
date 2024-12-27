package com.olelllka.chat_service.service.impl;

import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.repository.ChatRepository;
import com.olelllka.chat_service.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatRepository repository;

    @Override
    public Page<ChatEntity> getChatsForUser(String userId, Pageable pageable) {
        return repository.findChatsByUserId(userId, pageable);
    }

    @Override
    public ChatEntity createNewChat(String userId1, String userId2) {
        ChatEntity newChat = ChatEntity.builder()
                .participants(new String[]{userId1, userId2})
                .messages(List.of())
                .build();
        return repository.save(newChat);
    }
}
