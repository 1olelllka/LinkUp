package com.olelllka.profile_service.domain.dto;

import com.olelllka.profile_service.domain.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PatchProfileDto {
    @Pattern(regexp = "^.{8,}$", message = "Username must be from 8 characters")
    private String username;
    private String name;
    @Email(message = "Invalid Email.")
    private String email;
    private String aboutMe;
    private String photo;
    private Gender gender;
    @Past(message = "Date of birth must be in the past.")
    private LocalDate dateOfBirth;
}
