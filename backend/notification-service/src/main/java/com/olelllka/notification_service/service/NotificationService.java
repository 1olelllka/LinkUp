package com.olelllka.notification_service.service;

import com.olelllka.notification_service.domain.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    Page<NotificationEntity> getNotificationsForUser(UUID userId, Pageable pageable, String jwt);

    NotificationEntity updateReadNotification(String notificationId, String jwt);

    void deleteSpecificNotification(String notificationId, String jwt);

    void deleteNotificationsForSpecificUser(UUID userId, String jwt);
}
