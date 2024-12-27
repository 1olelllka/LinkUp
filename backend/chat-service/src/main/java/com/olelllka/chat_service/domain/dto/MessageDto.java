package com.olelllka.chat_service.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class MessageDto {
    private String id;
    private String to;
    private String from;
    @NotEmpty(message = "Message must not be empty.")
    private String content;
    private Date createdAt;
}