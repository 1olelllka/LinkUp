package com.olelllka.notification_service.service.impl;

import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import com.olelllka.notification_service.rest.exception.AuthException;
import com.olelllka.notification_service.rest.exception.NotFoundException;
import com.olelllka.notification_service.service.JWTUtil;
import com.olelllka.notification_service.service.NotificationService;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final JWTUtil jwtUtil;

    @Override
    public Page<NotificationEntity> getNotificationsForUser(UUID userId, Pageable pageable, String jwt) {
        authorize(userId, jwt);
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public NotificationEntity updateReadNotification(String notificationId, String jwt) {
        return repository.findById(notificationId).map(notification -> {
            authorize(notification.getUserId(), jwt);
            notification.setRead(true);
            return repository.save(notification);
        }).orElseThrow(() -> new NotFoundException("Notification with such id was not found."));
    }

    @Override
    public void deleteSpecificNotification(String notificationId, String jwt) {
        if (repository.existsById(notificationId)) {
            NotificationEntity entity = repository.findById(notificationId).get();
            authorize(entity.getUserId(), jwt);
            repository.deleteById(notificationId);
        }
    }

    @Override
    public void deleteNotificationsForSpecificUser(UUID userId, String jwt) {
        authorize(userId, jwt);
        repository.deleteByUserId(userId);
    }

    private void authorize(UUID resourceOwnerId, String jwt) {
        try {
            if (!jwtUtil.extractId(jwt).equals(resourceOwnerId.toString())) {
                throw new AuthException("You're unauthorized to perform this operation.");
            }
        } catch (SignatureException ex) {
            throw new AuthException(ex.getMessage());
        }
    }
}
