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
public class PostDto {
    private UUID user_id;
    private String image;
    private String desc;
    private Date created_at;
}
