package com.olelllka.profile_service.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Node("Profile")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfileEntity {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;
    private String username;
    private String name;
    private String aboutMe;
    private String photo;
    private Gender gender;
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private Set<ProfileEntity> following;
    @Relationship(type = "FOLLOWED_BY", direction = Relationship.Direction.INCOMING)
    private Set<ProfileEntity> followers;
    private LocalDate dateOfBirth;
    @CreatedDate
    private LocalDate createdAt;
}
