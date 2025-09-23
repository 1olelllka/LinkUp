package com.olelllka.notification_service.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.Date;
import java.util.UUID;

@Document("Notification")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationEntity {
    @Id
    private String id;
    @Field(targetType = FieldType.STRING)
    private UUID userId;
    private String text;
    private Boolean read;
    @CreatedDate
    private Date createdAt;
}
