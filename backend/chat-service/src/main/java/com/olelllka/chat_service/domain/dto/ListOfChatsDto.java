package com.olelllka.chat_service.domain.dto;

import com.olelllka.chat_service.domain.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Builder
@Data
public class ListOfChatsDto {
    private String id;
    private User[] participants;
    private String lastMessage;
    private Date time;
}
