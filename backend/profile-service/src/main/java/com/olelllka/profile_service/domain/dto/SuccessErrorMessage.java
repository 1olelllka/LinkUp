package com.olelllka.profile_service.domain.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SuccessErrorMessage {
    private String message;
}
