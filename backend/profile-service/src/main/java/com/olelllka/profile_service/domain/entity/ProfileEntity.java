package com.olelllka.profile_service.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.neo4j.core.schema.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Node("Profile")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileEntity {
    @Id
    @GeneratedValue
    private UUID id;
    private String username;
    private String password;
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
