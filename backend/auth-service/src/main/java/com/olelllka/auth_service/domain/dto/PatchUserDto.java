package com.olelllka.auth_service.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PatchUserDto {
    @Email(message = "Invalid Email.")
    private String email;
    @Pattern(regexp = "^\\w{8,}$", message = "Alias must be at least 8 characters and contain only letters, digits, or underscores.")
    private String alias;
}
