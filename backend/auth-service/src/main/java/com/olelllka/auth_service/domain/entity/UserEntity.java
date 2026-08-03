package com.olelllka.auth_service.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Document("User")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserEntity implements UserDetails {

    @Id
    @EqualsAndHashCode.Include
    private UUID userId;
    @Indexed(name = "email", unique = true)
    private String email;
    private String password;
    private Role role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(this.role.toString());
        return List.of(simpleGrantedAuthority);
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
