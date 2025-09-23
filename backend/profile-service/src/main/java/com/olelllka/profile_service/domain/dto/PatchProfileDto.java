package com.olelllka.profile_service.domain.dto;

import com.olelllka.profile_service.domain.entity.Gender;
import jakarta.validation.constraints.Past;
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
    private String name;
    private String aboutMe;
    private String photo;
    private Gender gender;
    @Past(message = "Date of birth must be in the past.")
    private LocalDate dateOfBirth;
}
