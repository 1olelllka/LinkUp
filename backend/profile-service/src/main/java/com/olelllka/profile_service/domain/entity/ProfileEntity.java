package com.olelllka.profile_service.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Set<ProfileEntity> following = new HashSet<>();
    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.INCOMING)
    private Set<ProfileEntity> followers = new HashSet<>();

    private LocalDate dateOfBirth;
    @CreatedDate
    private LocalDate createdAt;

    public void follow(ProfileEntity profile) {
        this.following.add(profile);
        profile.getFollowers().add(this);
    }

    public void unfollow(ProfileEntity profile) {
        this.following.remove(profile);
        profile.getFollowers().remove(profile);
    }
}
