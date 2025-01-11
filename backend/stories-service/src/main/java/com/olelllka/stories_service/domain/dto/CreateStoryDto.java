package com.olelllka.stories_service.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateStoryDto {
    @NotEmpty(message = "Image url must not be empty.")
    private String image;
}
