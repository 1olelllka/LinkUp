package com.olelllka.notification_service.repository;

import com.olelllka.notification_service.domain.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationEntity, String> {
    Page<NotificationEntity> findByUserId(UUID userId, Pageable pageable);
}
