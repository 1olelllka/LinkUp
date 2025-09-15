package com.olelllka.notification_service.service.impl;

import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.repository.NotificationRepository;
import com.olelllka.notification_service.rest.exception.AuthException;
import com.olelllka.notification_service.rest.exception.ForbiddenException;
import com.olelllka.notification_service.rest.exception.NotFoundException;
import com.olelllka.notification_service.service.JWTUtil;
import com.olelllka.notification_service.service.NotificationService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final JWTUtil jwtUtil;
    private final MongoTemplate mongoTemplate;

    @Override
    public Page<NotificationEntity> getNotificationsForUser(UUID userId, Pageable pageable, String jwt) {
        authorize(userId, jwt);
        return repository.findByUserId(userId, pageable);
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

    @Override
    public void updateReadNotifications(List<String> ids, String token) {
        String userId;
        try {
            userId = jwtUtil.extractId(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AuthException("You're unauthorized to perform this operation.");
        }
        Query query = new Query(Criteria.where("id").in(ids)).addCriteria(Criteria.where("userId").is(userId));
        List<NotificationEntity> results = mongoTemplate.query(NotificationEntity.class)
                .matching(query).all();
        if (results.size() == ids.size()) {
            Query updateQuery = new Query(Criteria.where("id").in(ids));
            Update update = new Update().set("read", true);
            mongoTemplate.update(NotificationEntity.class)
                    .matching(updateQuery).apply(update).all();
            return;
        }
        throw new ForbiddenException("You cannot perform this operation.");
    }

    private void authorize(UUID resourceOwnerId, String jwt) {
        try {
            if (!jwtUtil.extractId(jwt).equals(resourceOwnerId.toS