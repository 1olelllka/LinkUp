package com.olelllka.notification_service.rest.controller;

import com.olelllka.notification_service.domain.dto.NotificationDto;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<NotificationDto>> getListOfNotificationsForUser(@PathVariable UUID user_id,
                                                                               @RequestHeader(name="Authorization") String header,
                                                                               Pageable pageable) {
        Page<NotificationEntity> entities = service.getNotificationsForUser(user_id, pageable, header.substring(7));
        Page<NotificationDto> result = entities.map(this::notificationMapper);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PatchMapping("/read")
    public ResponseEntity updateReadStatus(@RequestParam(name="ids") List<String> ids,
                                           @RequestHeader(name="Authorization") String header) {
        service.updateReadNotifications(ids, header.substring(7));
        return new ResponseEntity(HttpStatus.OK);
    }

    @DeleteMapping("/{notification_id}")
    public ResponseEntity deleteSpecificNotification(@PathVariable String notification_id,
                                                     @RequestHeader(name="Authorization") String header) {
        service.deleteSpecificNotification(notification_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/users/{user_id}")
    public ResponseEntity deleteAllOfTheNotificationsForSpecificUser(@PathVariable UUID user_id,
                                                                     @RequestHeader(name="Authorization") String header) {
        service.deleteNotificationsForSpecificUser(user_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    private NotificationDto notificationMapper(NotificationEntity entity) {
        return NotificationDto.builder()
                .id(entity.getId())
                .read(entity.getRead())
                .text(entity.getText())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUserId().toString())
                .build();
    }
}
