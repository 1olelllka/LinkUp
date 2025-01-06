package com.olelllka.profile_service.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import java.util.UUID;

@Document(indexName = "profile")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileDocument {
    @Id
    @GeneratedValue
    private UUID id;
    private String username;
    private String name;
    private String email;
}
