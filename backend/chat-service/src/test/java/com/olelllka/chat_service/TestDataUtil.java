package com.olelllka.chat_service;

import com.olelllka.chat_service.domain.dto.ChatDto;
import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.domain.entity.User;

import java.util.Date;
import java.util.UUID;

public class TestDataUtil {

    public static MessageEntity createMessageEntity(String chatId) {
        return MessageEntity.builder()
                .to(UUID.randomUUID())
                .from(UUID.randomUUID())
                .content("Hello World")
                .chatId(chatId)
                .build();
    }

    public static MessageDto createMessageDto(String chatId) {
        return MessageDto.builder()
                .to(UUID.randomUUID())
                .from(UUID.randomUUID())
                .content("Hello World")
                .chatId(chatId)
                .build();
    }

    public static User createUser(UUID id) {
        return User.builder().id(id).name("Random1").username("random1").build();
    }

    public static ChatEntity createChatEntity() {
        UUID[] participants = {UUID.randomUUID(), UUID.randomUUID()};
        return ChatEntity.builder()
                .participants(new User[]
                        {createUser(participants[0]),
                        createUser(participants[1])})
                .lastMessage("test")
                .time(new Date())
                .build();
    }

    public static ChatDto createChatDto() {
        UUID[] participants = {UUID.randomUUID(), UUID.randomUUID()};
        return ChatDto.builder()
                .participants(new User[]
                        {createUser(participants[0]),
                        createUser(participants[1])})
                .lastMessage("test")
                .time(new Date())
                .build();
    }

}
