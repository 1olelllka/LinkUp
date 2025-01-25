package com.olelllka.notification_service.service.impl;

import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.feign.ProfileFeign;
import com.olelllka.notification_service.repository.NotificationRepository;
import com.olelllka.notification_service.rest.exception.NotFoundException;
import com.olelllka.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final MongoTemplate mongoTemplate;
    private final ProfileFeign profileService;

    @Override
    public Page<NotificationEntity> getNotificationsForUser(UUID userId, Pageable pageable) {
        if (!profileService.getProfileById(userId).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("User with such id does not exist.");
        }
        return repository.findByUserId(userId, pageable);
    }

    @Override
    public NotificationEntity updateReadNotification(String notificationId) {
        return repository.findById(notificationId).map(notification -> {
            notification.setRead(true);
            return repository.save(notification);
        }).orElseThrow(() -> new NotFoundException("Notification with such id was not found."));
    }

    @Override
    public void deleteSpecificNotification(String notificationId) {
        repository.deleteById(notificationId);
    }

    @Override
    public void deleteNotificationsForSpecificUser(UUID userId) {
        if (!profileService.getProfileById(userId).getStatusCode().is2xxSuccessful()) {
            throw new NotFoundException("User with such id does not exist.");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("userId").is(userId));
        mongoTemplate.findAllAndRemove(query, NotificationEntity.class);
    }
}
