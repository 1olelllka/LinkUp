package com.olelllka.chat_service.domain.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Chat")
@Builder
@Data
public class ChatEntity {
    @Id
    private String id;
    private User[] participants;
    private String lastMessage;
    private Date time;
}
