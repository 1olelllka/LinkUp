package com.olelllka.profile_service.domain.dto;

import com.olelllka.profile_service.domain.entity.Gender;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
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
    private Set<ProfileEntity> following = new HashSet<>();
    private Set<ProfileEntity> followers = new HashSet<>();
    private LocalDate dateOfBirth;
    private LocalDate createdAt;
}
