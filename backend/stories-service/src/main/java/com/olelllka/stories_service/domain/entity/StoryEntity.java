package com.olelllka.stories_service.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.UUID;

@Document(collection = "Story")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoryEntity {

    @Id
    private String id;
    private String image;
    private UUID userId;
    @Builder.Default
    private Integer likes = 0;
    private Boolean available;
    @Builder.Default
    private Date createdAt = new Date();
}
