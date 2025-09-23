package com.olelllka.profile_service.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FollowDto {
    @NotNull(message = "Follower id must not be empty.")
    private UUID followerId;
    @NotNull(message = "Followee id must not be empty.")
    private UUID followeeId;
}
