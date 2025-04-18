package com.olelllka.auth_service.service.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.olelllka.auth_service.domain.dto.Gender;
import com.olelllka.auth_service.domain.dto.JWTToken;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.AuthProvider;
import com.olelllka.auth_service.domain.entity.Role;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.service.JWTUtil;
import com.olelllka.auth_service.service.MessagePublisher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final MessagePublisher messagePublisher;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        UserEntity user = userRepository.findByEmail(email).orElseGet(() -> {
            UUID newProfileId = UUID.randomUUID();
            Faker faker = new Faker();
            String username = faker.superhero().prefix()+faker.name().firstName()+faker.address().buildingNumber();
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .providerId(providerId)
                    .authProvider(AuthProvider.GOOGLE)
                    .role(Role.USER)
                    .alias(username)
                    .userId(newProfileId)
                    .build();
            UserEntity savedUser = userRepository.save(newUser);
            UserMessageDto toSend = UserMessageDto.builder()
                    .gender(Gender.UNDEFINED)
                    .email(savedUser.getEmail())
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .username(username)
                    .profileId(newProfileId)
                    .name(name)
                    .build();
            messagePublisher.sendCreateUserMessage(toSend);
            return savedUser;
        });
        String jwtToken = jwtUtil.generateJWT(user.getUserId());
        JWTToken jwt = JWTToken.builder().token(jwtToken).build();
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(objectMapper.writeValueAsString(jwt));
        response.setContentType("application/json");
    }
}
