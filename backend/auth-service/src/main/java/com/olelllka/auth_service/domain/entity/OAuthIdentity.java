package com.olelllka.auth_service.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document("OAuthIdentity")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@CompoundIndex(def = "{'authProvider':1, 'providerSubject': 1}", unique = true)
public class OAuthIdentity {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private AuthProvider authProvider;
    private String providerSubject;
}
