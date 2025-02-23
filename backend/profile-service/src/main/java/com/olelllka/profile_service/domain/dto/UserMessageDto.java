package com.olelllka.profile_service.domain.dto;

import com.olelllka.profile_service.domain.entity.Gender;
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