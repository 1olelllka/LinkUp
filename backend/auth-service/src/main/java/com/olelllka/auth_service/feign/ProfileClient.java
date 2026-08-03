package com.olelllka.auth_service.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "PROFILE-SERVICE", url = "http://${PROFILE_HOST:localhost}:8001", dismiss404 = true)
public interface ProfileClient {
    @GetMapping("/profiles/username-availability")
    ResponseEntity<?> getUsernameAvailability(@RequestParam(name = "username") String username);
}