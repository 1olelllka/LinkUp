package com.olelllka.notification_service.service;

import com.olelllka.notification_service.domain.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    Page<NotificationEntity> getNotificationsForUser(UUID userId, Pageable pageable);

    NotificationEntity updateReadNotification(String notificationId);

    void deleteSpecificNotification(String notificationId);

    void deleteNotificationsForSpecificUser(UUID userId);
}
