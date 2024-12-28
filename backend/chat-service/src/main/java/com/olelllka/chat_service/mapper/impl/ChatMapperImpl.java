package com.olelllka.chat_service.mapper.impl;

import com.olelllka.chat_service.domain.dto.ChatDto;
import com.olelllka.chat_service.domain.entity.ChatEntity;
import com.olelllka.chat_service.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatMapperImpl implements Mapper<ChatEntity, ChatDto> {

    @Autowired
    private MessageMapperImpl messageMapper;

    @Override
    public ChatEntity toEntity(ChatDto dto) {
        return ChatEntity.builder()
                .participants(dto.getParticipants())
                .build();
    }

    @Override
    public ChatDto toDto(ChatEntity entity) {
        return ChatDto.builder()
                .participants(entity.getParticipants())
                .build();
    }
}
