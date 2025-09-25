package com.olelllka.stories_service.feign;

import com.olelllka.stories_service.domain.dto.ProfileDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(value = "PROFILE-SERVICE", url = "http://${PROFILE_HOST:localhost}:8001", dismiss404 = true)
public interface ProfileFeign {
    @GetMapping("/profiles/{profile_id}")
    ResponseEntity<?> getProfileById(@PathVariable UUID profile_id);

    @GetMapping("/profiles/{profile_id}/followees")
    ResponseEntity<Page<ProfileDto>> getAllFolloweesForProfile(@PathVariable UUID profile_id, Pageable pageable);
}
