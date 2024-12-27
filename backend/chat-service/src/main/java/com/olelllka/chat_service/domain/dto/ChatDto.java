package com.olelllka.chat_service.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ChatDto {
    private String id;
    private String[] participants;
    private List<MessageDto> messages;
}
