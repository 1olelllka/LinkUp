package com.olelllka.auth_service.domain.dto;

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
public class RegisterUserDto {
    @Email(message = "Invalid Email.")
    @NotEmpty(message = "Email must not be empty.")
    private String email;
    @NotBlank(message = "Username must not be blank.")
    @Pattern(regexp = "^\\w{8,}$", message = "Alias must be at least 8 characters and contain only letters, digits, or underscores.")
    private String alias;
    @NotBlank(message = "Password must not be blank.")
    @Pattern(regexp = "^\\w{8,}$", message = "Password must be at least 8 characters and contain only letters, digits, or underscores.")
    private String password;
    @NotBlank(message = "Name must not be blank.")
    private String name;
    @NotNull(message = "Gender must not be null.")
    private Gender gender;
    @Past(message = "Date of birth must be in past.")
    @NotNull(message = "Date of birth must not be empty.")
    private LocalDate dateOfBirth;
}
