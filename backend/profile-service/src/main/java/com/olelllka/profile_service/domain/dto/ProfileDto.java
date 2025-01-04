package com.olelllka.profile_service.domain.dto;

import com.olelllka.profile_service.domain.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileDto {
    private UUID id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String aboutMe;
    private String photo;
    private Gender gender;
    private List<UUID> following;
    private List<UUID> followers;
    private LocalDate dateOfBirth;
    private LocalDate createdAt;
}
