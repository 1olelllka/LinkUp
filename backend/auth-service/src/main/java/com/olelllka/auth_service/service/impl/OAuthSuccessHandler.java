package com.olelllka.auth_service.service.impl;


import com.github.javafaker.Faker;
import com.olelllka.auth_service.domain.dto.Gender;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.AuthProvider;
import com.olelllka.auth_service.domain.entity.OAuthIdentity;
import com.olelllka.auth_service.domain.entity.Role;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.OAuthIdentityRepository;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.service.JWTUtil;
import com.olelllka.auth_service.service.MessagePublisher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final MessagePublisher messagePublisher;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final OAuthIdentityRepository oAuthIdentityRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        log.info(oAuth2User.toString());
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");
        // FOR NOW GOOGLE, CHANGE TO GENERIC LATER
        OAuthIdentity identity = oAuthIdentityRepository.findByAuthProviderAndProviderSubject(AuthProvider.GOOGLE, providerId).orElseGet(() -> {
            UUID newProfileId = UUID.randomUUID();
            Faker faker = new Faker();
            UserEntity newUser = UserEntity.builder()
                    .email(email)
                    .role(Role.USER)
                    .userId(newProfileId)
                    .build();
            UserEntity savedUser = userRepository.save(newUser);
            OAuthIdentity newIdentity = OAuthIdentity
                    .builder()
                    .id(savedUser.getUserId())
                    .authProvider(AuthProvider.GOOGLE)
                    .providerSubject(providerId)
                    .build();
            oAuthIdentityRepository.save(newIdentity);
            String username = faker.superhero().prefix()+faker.name().firstName()+faker.address().buildingNumber();
            UserMessageDto toSend = UserMessageDto.builder()
                    .gender(Gender.UNDEFINED)
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .username(username)
                    .profileId(newProfileId)
                    .name(name)
                    .build();
            messagePublisher.sendCreateUserMessage(toSend);
            return newIdentity;
        });
        String refreshJWT = jwtUtil.generateRefreshJWT(identity.getId());
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshJWT)
                        .httpOnly(true)
                        .maxAge(3600 * 24)
                        .sameSite("Strict")
                        .path("/")
                        .build();
        redisTemplate.opsForValue().set("refresh_token:" + cookie.getValue(), "", Duration.of(1, ChronoUnit.DAYS));
        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        response.setStatus(HttpServletResponse.SC_OK);
        response.sendRedirect("http://localhost:5173/profile?pending=true");
    }
}
