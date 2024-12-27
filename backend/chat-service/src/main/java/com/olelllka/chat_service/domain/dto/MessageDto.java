package com.olelllka.chat_service.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class MessageDto {
    private String id;
    private String to;
    private String from;
    private String content;
    private Date createdAt;
}