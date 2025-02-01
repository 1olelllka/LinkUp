package com.olelllka.feed_service;

import com.olelllka.feed_service.domain.dto.NewPostEvent;
import com.olelllka.feed_service.domain.dto.PostDto;
import com.olelllka.feed_service.domain.dto.ProfileDto;

import java.util.Date;
import java.util.UUID;

public class TestDataUtil {

    public static PostDto createPostDto(UUID profile_id) {
        return PostDto.builder()
                .user_id(profile_id)
                .image("img")
                .desc("desc")
                .created_at(new Date())
                .build();
    }

    public static NewPostEvent createNewPostEvent(UUID profileId) {
        return NewPostEvent.builder()
                .postId("post_id")
                .profileId(profileId)
                .timeStamp(new Date())
                .build();
    }

    public static ProfileDto createProfileDto(UUID profileId) {
        return ProfileDto.builder()
                .id(profileId)
                .name("Name")
                .photo("photo-url")
                .build();
    }
}
