package com.olelllka.feed_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NewPostEvent {
    private String postId;
    private UUID profileId;
    private Date timeStamp;
}
