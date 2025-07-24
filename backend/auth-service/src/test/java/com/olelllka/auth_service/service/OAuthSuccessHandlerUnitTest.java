package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.dto.UserMessageDto;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.service.impl.OAuthSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OAuthSuccessHandlerUnitTest {

    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private MessagePublisher messagePublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oAuth2User;
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @InjectMocks
    private OAuthSuccessHandler oAuthSuccessHandler;

    @Test
    public void testThatItWorksIfUserAlreadyExists() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        UserEntity user = TestDataUtil.createUserEntity();
        UUID id = UUID.randomUUID();
        user.setUserId(id);
        // when
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(user.getEmail());
        when(oAuth2User.getAttribute("name")).thenReturn("name");
        when(oAuth2User.getAttribute("sub")).thenReturn("2345");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateAccessJWT(id)).thenReturn("ACCESS_TOKEN");
        when(jwtUtil.generateRefreshJWT(id)).thenReturn("REFRESH_TOKEN");
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        oAuthSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        // then
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("REFRESH_TOKEN", response.getCookie("refresh_token").getValue());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(messagePublisher, never()).sendCreateUserMessage(any(UserMessageDto.class));
        verify(redisTemplate.opsForValue(), times(1)).set("refresh_token:REFRESH_TOKEN", "", Duration.of(1, ChronoUnit.DAYS));
    }

    @Test
    public void testThatItWorksIfUserIsNew() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String email = "email@email.com";
        String name = "name";
        String sub = "2345";
        // when
        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn(name);
        when(oAuth2User.getAttribute("sub")).thenReturn(sub);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateAccessJWT(any(UUID.class))).thenReturn("ACCESS_TOKEN");
        when(jwtUtil.generateRefreshJWT(any(UUID.class))).thenReturn("REFRESH_TOKEN");
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        oAuthSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        // then
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("REFRESH_TOKEN", response.getCookie("refresh_token").getValue());
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(messagePublisher, times(1)).sendCreateUserMessage(any(UserMessageDto.class));
        verify(redisTemplate.opsForValue()).set("refresh_token:REFRESH_TOKEN", "", Duration.of(1, ChronoUnit.DAYS));
    }

}
