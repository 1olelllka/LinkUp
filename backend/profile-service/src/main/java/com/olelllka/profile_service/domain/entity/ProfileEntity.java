package com.olelllka.profile_service.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Node("Profile")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class ProfileEntity {
    @Id
    private UUID id;
    private String username;
    private String name;
    private String email;
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
