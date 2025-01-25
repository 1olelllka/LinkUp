package com.olelllka.chat_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(value = "PROFILE-SERVICE", dismiss404 = true)
public interface ProfileFeign {
    @GetMapping("/profiles/{profile_id}")
    ResponseEntity<?> getProfileById(@PathVariable UUID profile_id);
}
