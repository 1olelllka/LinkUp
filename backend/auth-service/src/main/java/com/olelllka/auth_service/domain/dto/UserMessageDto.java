package com.olelllka.auth_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserMessageDto {
    private UUID profileId;
    private String email;
    private String username;
    private String name;
    private Gender gender;
    private LocalDate dateOfBirth;
}
