package com.olelllka.notification_service.rest.controller;

import com.olelllka.notification_service.domain.dto.NotificationDto;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/notifications")
public class NotificationController {

    @Autowired
    private NotificationService service;

    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<NotificationDto>> getListOfNotificationsForUser(@PathVariable UUID user_id,
                                                                               Pageable pageable) {
        Page<NotificationEntity> entities = service.getNotificationsForUser(user_id, pageable);
        Page<NotificationDto> result = entities.map(this::notificationMapper);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping("/{notification_id}")
    public ResponseEntity<NotificationDto> updateReadNotification(@PathVariable String notification_id) {
        NotificationEntity updated = service.updateReadNotification(notification_id);
        return new ResponseEntity<>(notificationMapper(updated), HttpStatus.OK);
    }

    @DeleteMapping("/{notification_id}")
    public ResponseEntity deleteSpecificNotification(@PathVariable String notification_id) {
        service.deleteSpecificNotification(notification_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/users/{user_id}")
    public ResponseEntity deleteAllOfTheNotificationsForSpecificUser(@PathVariable UUID user_id) {
        service.deleteNotificationsForSpecificUser(user_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private NotificationDto notificationMapper(NotificationEntity entity) {
        return NotificationDto.builder()
                .id(entity.getId())
                .read(entity.getRead())
                .text(entity.getText())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUserId())
                .build();
    }
}
