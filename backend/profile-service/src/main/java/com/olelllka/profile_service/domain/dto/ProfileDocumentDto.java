package com.olelllka.profile_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileDocumentDto {
    private UUID id;
    private String username;
    private String name;
    private String photo;
    private String email;
}
