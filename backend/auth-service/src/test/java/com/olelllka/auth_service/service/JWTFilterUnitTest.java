package com.olelllka.auth_service.service;

import com.olelllka.auth_service.TestDataUtil;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JWTFilterUnitTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JWTUtil jwtUtil;
    @InjectMocks
    private JWTFilter jwtFilter;


    @Test
    public void testThatIfHeaderIsIncorrectFunctionsWillNotBeExecuted() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();
        // when
        jwtFilter.doFilterInternal(request, response, filterChain);
        // then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(jwtUtil, never()).isTokenValid(anyString(), anyString());
    }

    @Test
    public void testThatIfJwtIsExpiredFurtherFunctionsWillNotBeExecuted() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();
        // when
        when(jwtUtil.extractUsername("TOKEN")).thenThrow(new JwtException(""));
        jwtFilter.doFilterInternal(request, response, filterChain);
        // then
        assertEquals(response.getStatus(), HttpServletResponse.SC_UNAUTHORIZED);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtUtil, never()).isTokenValid(anyString(), anyString());
    }

    @Test
    public void testThatFilterThrowsExceptionIfUserWasNotFound() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();
        // when
        when(jwtUtil.extractUsername("TOKEN")).thenReturn("email@email.com");
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.empty());
        // then
        assertThrows(UnauthorizedException.class, () -> jwtFilter.doFilterInternal(request, response, filterChain));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).isTokenValid(anyString(), anyString());
    }

    @Test
    public void testThatFilterPerformsAuthorization() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer TOKEN");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = new MockFilterChain();
        // when
        when(jwtUtil.extractUsername("TOKEN")).thenReturn("email@email.com");
        when(userRepository.findByEmail("email@email.com")).thenReturn(Optional.of(TestDataUtil.createUserEntity()));
        when(jwtUtil.isTokenValid("email@email.com", "TOKEN")).thenReturn(true);
        jwtFilter.doFilterInternal(request, response, filterChain);
        // then
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        // tearDown
        SecurityContextHolder.getContext().setAuthentication(null);
    }

}
