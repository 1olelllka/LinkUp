package com.olelllka.profile_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationDto {
    private String id;
    private String userId;
    private String text;
    private Boolean read;
    private Date createdAt;
}
