package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTFilterUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JWTFilter jwtFilter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldSkip_whenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldSkip_whenHeaderDoesNotStartWithBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic somecreds");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtUtil, userRepository);
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenValidAndUserFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String jwt = "valid.jwt.token";
        UserEntity user = TestDataUtil.createUserEntity();
        user.setUserId(userId);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.isTokenValid(userId, jwt)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        assertEquals(user.getEmail(), principal.getUsername());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotSetAuthentication_whenTokenInvalid() throws Exception {
        UUID userId = UUID.randomUUID();
        String jwt = "valid.jwt.token";
        UserEntity user = TestDataUtil.createUserEntity();
        user.setUserId(userId);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtUtil.isTokenValid(userId, jwt)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldNotOverwriteExistingAuthentication() throws Exception {
        UUID userId = UUID.randomUUID();
        String jwt = "valid.jwt.token";

        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "existing-user", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(userRepository);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldWriteUnauthorized_whenUserNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        String jwt = "valid.jwt.token";

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractId(jwt)).thenReturn(userId.toString());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(response.getWriter()).thenReturn(printWriter);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        printWriter.flush();
        assertTrue(stringWriter.toString().contains("Unauthorized"));

        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_shouldWriteErrorResponse_whenTokenMalformed() throws Exception {
        String jwt = "garbage-token";

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + jwt);
        when(jwtUtil.extractId(jwt)).thenThrow(new RuntimeException("malformed jwt"));
        when(response.getWriter()).thenReturn(printWriter);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        printWriter.flush();
        assertTrue(stringWriter.toString().contains("malformed jwt"));

        verify(filterChain, never()).doFilter(any(), any());
    }
}