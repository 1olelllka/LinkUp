package com.olelllka.chat_service.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CreateChatDto {
    @NotEmpty(message = "1st user id must not be empty")
    private String user1Id;
    @NotEmpty(message = "2nd user id must not be empty")
    private String user2Id;
}
