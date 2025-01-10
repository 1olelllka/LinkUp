package com.olelllka.notification_service.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private UUID userId;
    private String text;
    private Boolean read;
    @CreatedDate
    private Date createdAt;
}
