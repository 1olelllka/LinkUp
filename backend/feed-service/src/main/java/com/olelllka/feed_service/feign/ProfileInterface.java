package com.olelllka.feed_service.feign;

import com.olelllka.feed_service.domain.dto.ProfileDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(value = "PROFILE-SERVICE", url = "http://localhost:8001", dismiss404 = true)
public interface ProfileInterface {

    @GetMapping("/profiles/{profile_id}/followers")
    ResponseEntity<Page<ProfileDto>> getAllFollowersForProfile(@PathVariable UUID profile_id, Pageable pageable);

    @GetMapping("/profiles/{profile_id}")
    ResponseEntity<?> getProfileById(@PathVariable UUID profile_id);
}