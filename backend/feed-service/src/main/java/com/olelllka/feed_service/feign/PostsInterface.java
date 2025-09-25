package com.olelllka.feed_service.feign;

import com.olelllka.feed_service.domain.dto.PostDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "POSTS-SERVICE", url = "http://${POSTS_HOST:localhost}:8000", dismiss404 = true)
public interface PostsInterface {

    @GetMapping("/posts/{id}")
    ResponseEntity<PostDto> getPosts(@PathVariable Integer id);
}
