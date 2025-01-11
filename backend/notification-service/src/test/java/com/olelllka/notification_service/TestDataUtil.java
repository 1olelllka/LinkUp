package com.olelllka.notification_service;

import com.olelllka.notification_service.domain.dto.NotificationDto;
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

    public static NotificationDto createNotificationDto() {
        return NotificationDto.builder()
                .read(false)
                .text("Simple text")
                .userId(UUID.randomUUID().toString())
                .build();
    }
}
