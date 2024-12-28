package com.olelllka.chat_service.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class MessageDto {
    private String id;
    private String chatId;
    private String to;
    private String from;
    @NotEmpty(message = "Message must not be empty.")
    private String content;
    private Date createdAt;
}