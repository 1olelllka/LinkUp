package com.olelllka.chat_service.domain.entity;


import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "Message")
@Builder
@Data
public class MessageEntity {
    @Id
    private String id;
    private String to;
    private String from;
    private String content;
    private Date createdAt;

    public void setIdIfNotPresent() {
        if (this.id == null) {
            this.id = new ObjectId().toHexString();
        }
    }
}
