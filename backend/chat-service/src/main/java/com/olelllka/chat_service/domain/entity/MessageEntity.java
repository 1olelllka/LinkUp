package com.olelllka.chat_service.domain.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Message")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageEntity {
    @Id
    private String id;
    private String chatId;
    private String to;
    private String from;
    private String content;
    private Date createdAt;
}
