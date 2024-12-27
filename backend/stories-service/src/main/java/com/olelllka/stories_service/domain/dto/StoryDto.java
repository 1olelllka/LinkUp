package com.olelllka.stories_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class StoryDto {
    private String id;
    private String image;
    private String userId;
    private Integer likes;
    private Boolean available;
    private Date createdAt;
}
