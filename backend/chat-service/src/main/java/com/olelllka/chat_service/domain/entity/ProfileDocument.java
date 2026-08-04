package com.olelllka.chat_service.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("Profile")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfileDocument {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private String name;
    private String username;
    private String photo;
}
