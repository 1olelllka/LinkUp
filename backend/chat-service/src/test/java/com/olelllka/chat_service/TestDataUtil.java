package com.olelllka.chat_service;

import com.olelllka.chat_service.domain.dto.ChatDto;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;

import java.util.Date;
import java.util.List;

public class TestDataUtil {

    public static MessageEntity createMessageEntity(String chatId) {
        return MessageEntity.builder()
                .to("User A")
                .from("User B")
                .content("Hello World")
                .chatId(chatId)
                .build();
    }

    public static MessageDto createMessageDto(String chatId) {
        return MessageDto.builder()
                .to("User A")
                .from("User B")
                .content("Hello World")
                .chatId(chatId)
                .build();
    }

    public static ChatEntity createChatEntity() {
        String[] participants = {"1234", "5678"};
        return ChatEntity.builder()
                .participants(participants)
                .build();
    }

    public static ChatDto createChatDto() {
        String[] participants = {"1234", "5678"};
        return ChatDto.builder()
                .participants(participants)
                .build();
    }

}
