package com.olelllka.chat_service.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class ChatDto {
    private String id;
    private UUID[] participants;
}
