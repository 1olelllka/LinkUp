package com.olelllka.chat_service.domain.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ListOfChatsDto {
    private String id;
    private String[] participants;
}
