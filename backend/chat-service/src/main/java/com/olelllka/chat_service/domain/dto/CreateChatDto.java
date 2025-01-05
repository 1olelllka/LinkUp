package com.olelllka.chat_service.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Builder
@Data
public class CreateChatDto {
    @NotNull(message = "1st user id must not be empty")
    private UUID user1Id;
    @NotNull(message = "2nd user id must not be empty")
    private UUID user2Id;
}
