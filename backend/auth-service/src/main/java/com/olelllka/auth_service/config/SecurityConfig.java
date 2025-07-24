package com.olelllka.auth_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olelllka.auth_service.domain.dto.ErrorMessage;
import com.olelllka.auth_service.repository.UserRepository;
import com.olelllka.auth_service.rest.exception.NotFoundException;
import com.olelllka.auth_service.service.JWTFilter;
import com.olelllka.auth_service.service.impl.OAuthSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    final JWTFilter jwtFilter;
    final AuthenticationProvider provider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, OAuthSuccessHandler oAuthSuccessHandler) throws Exception {
        http
                .csrf(CsrfConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/auth/register", "/auth/login", "/auth/oauth2/**", "/actuator/**", "/auth/refresh").permitAll()
                            .anyRequest().authenticated();
                })
                .oauth2Login(login ->
                        login.authorizationEndpoint(endpoint -> endpoint.baseUri("/auth/oauth2/authorization"))
                                .successHandler(oAuthSuccessHandler))
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint((req, res, authEx) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ObjectMapper objectMapper = new ObjectMapper();
                            ErrorMessage errorMessage = ErrorMessage.builder().message("Access Denied").build();
                            res.getWriter().write(objectMapper.writeValueAsString(errorMessage));
                            res.setContentType("application/json");
                        }))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(provider)
                .logout(logout -> logout.logoutUrl("/auth/logout")
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(HttpServletResponse.SC_OK);
                        })
                        .clearAuthentication(true)
                        .invalidateHttpSession(true));
        return http.build();
    }
}
