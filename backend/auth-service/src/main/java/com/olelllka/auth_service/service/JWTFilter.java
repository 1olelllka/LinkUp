package com.olelllka.auth_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.auth_service.domain.dto.ErrorMessage;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import io.jsonwebtoken.ExpiredJwtException;
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

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getHeader("Authorization") == null || !request.getHeader("Authorization").startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = request.getHeader("Authorization").substring(7);
        String email = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            email = jwtUtil.extractUsername(jwt);
        } catch (Exception ex) {
            ErrorMessage errorMessage = ErrorMessage.builder().message(ex.getMessage()).build();
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(errorMessage));
            filterChain.doFilter(request, response);
            return;
        }
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Unauthorized."));
            if (jwtUtil.isTokenValid(email, jwt)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
