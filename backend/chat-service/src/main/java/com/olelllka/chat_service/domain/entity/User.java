package com.olelllka.chat_service.domain.entity;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class User {
    private UUID id;
    private String username;
    private String name;
}
