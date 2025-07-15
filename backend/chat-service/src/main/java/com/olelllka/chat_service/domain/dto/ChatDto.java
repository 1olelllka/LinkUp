package com.olelllka.chat_service.domain.dto;

import com.olelllka.chat_service.domain.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class ChatDto {
    private String id;
    private User[] participants;
}
