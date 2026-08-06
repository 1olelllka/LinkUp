package com.olelllka.auth_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.auth_service.domain.dto.ErrorMessage;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = request.getHeader("Authorization").substring(7);
        UUID userId;
        try {
            userId = UUID.fromString(jwtUtil.extractId(jwt));
        } catch (Exception ex) {
            writeUnauthorized(response, ex.getMessage());
            return;
        }
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userRepository.findById(userId)
                        .orElseThrow(() -> new UnauthorizedException("Unauthorized."));
                if (jwtUtil.isTokenValid(userId, jwt)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (UnauthorizedException ex) {
                writeUnauthorized(response, ex.getMessage());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        ErrorMessage errorMessage = ErrorMessage.builder().message(message).build();
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorMessage));
    }
}