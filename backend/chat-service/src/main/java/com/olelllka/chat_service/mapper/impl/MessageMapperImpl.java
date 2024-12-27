package com.olelllka.chat_service.mapper.impl;

import com.olelllka.chat_service.domain.dto.MessageDto;
import com.olelllka.chat_service.domain.entity.MessageEntity;
import com.olelllka.chat_service.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class MessageMapperImpl implements Mapper<MessageEntity, MessageDto> {
    @Override
    public MessageEntity toEntity(MessageDto dto) {
        return MessageEntity.builder()
                .to(dto.getTo())
                .from(dto.getFrom())
                .content(dto.getContent())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    @Override
    public MessageDto toDto(MessageEntity entity) {
        return MessageDto.builder()
                .to(entity.getTo())
                .from(entity.getFrom())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
