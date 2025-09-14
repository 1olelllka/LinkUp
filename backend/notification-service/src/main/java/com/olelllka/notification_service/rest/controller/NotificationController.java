package com.olelllka.notification_service.rest.controller;

import com.olelllka.notification_service.domain.dto.ErrorMessage;
import com.olelllka.notification_service.domain.dto.NotificationDto;
import com.olelllka.notification_service.domain.entity.NotificationEntity;
import com.olelllka.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notifications Service API Endpoints", description = "All endpoints for notifications service")
public class NotificationController {

    private final NotificationService service;

    @Operation(summary = "Get list of notifications for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched list of notifications"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @GetMapping("/users/{user_id}")
    public ResponseEntity<Page<NotificationDto>> getListOfNotificationsForUser(@PathVariable UUID user_id,
                                                                               @RequestHeader(name="Authorization") String header,
                                                                               Pageable pageable) {
        Page<NotificationEntity> entities = service.getNotificationsForUser(user_id, pageable, header.substring(7));
        Page<NotificationDto> result = entities.map(this::notificationMapper);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Update read status for list of notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated statuses"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Forbidden to perform operation", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @PatchMapping("/read")
    public ResponseEntity updateReadStatus(@RequestParam(name="ids") List<String> ids,
                                           @RequestHeader(name="Authorization") String header) {
        service.updateReadNotifications(ids, header.substring(7));
        return new ResponseEntity(HttpStatus.OK);
    }

    @Operation(summary = "Delete single notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted notification"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @DeleteMapping("/{notification_id}")
    public ResponseEntity deleteSpecificNotification(@PathVariable String notification_id,
                                                     @RequestHeader(name="Authorization") String header) {
        service.deleteSpecificNotification(notification_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Delete all notifications")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted notifications"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
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
