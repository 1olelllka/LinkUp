package com.olelllka.auth_service.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginUser {
    @Email(message = "Invalid Email.")
    @NotEmpty(message = "Email must not be empty.")
    private String email;
    @NotEmpty(message = "Password must not be blank.")
    private String password;
}
