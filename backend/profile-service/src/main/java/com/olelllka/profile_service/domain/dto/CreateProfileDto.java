package com.olelllka.profile_service.domain.dto;

import com.olelllka.profile_service.domain.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class CreateProfileDto {
    @NotBlank(message = "Username must not be blank.")
    @Pattern(regexp = "^\\w{8,}$", message = "Username must be at least 8 characters and contain only letters, digits, or underscores.")
    private String username;
    @NotBlank(message = "Password must not be blank.") // for now
    private String password;
    @NotEmpty(message = "Name must be not empty.")
    private String name;
    @Email(message = "Invalid Email.")
    @NotEmpty(message = "Email must not be empty.")
    private String email;
    @NotNull(message = "Gender must not be empty.")
    private Gender gender;
    @Past(message = "Date of birth must be in the past.")
    @NotNull(message = "Date of birth must not be empty.")
    private LocalDate dateOfBirth;
}
