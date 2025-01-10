package com.olelllka.notification_service;

import com.olelllka.notification_service.domain.entity.NotificationEntity;

import java.util.UUID;

public class TestDataUtil {

    public static NotificationEntity createNotificationEntity() {
        return NotificationEntity.builder()
                .read(false)
                .text("Simple text")
                .userId(UUID.randomUUID())
                .build();
    }
}
