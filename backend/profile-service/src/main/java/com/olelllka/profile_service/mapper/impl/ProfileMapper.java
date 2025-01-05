package com.olelllka.profile_service.mapper.impl;

import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.mapper.Mapper;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper implements Mapper<ProfileEntity, ProfileDto> {
    @Override
    public ProfileEntity toEntity(ProfileDto profileDto) {
        return ProfileEntity
                .builder()
                .id(profileDto.getId())
                .username(profileDto.getUsername())
                .email(profileDto.getEmail())
                .password(profileDto.getPassword())
                .name(profileDto.getName())
                .aboutMe(profileDto.getAboutMe())
                .photo(profileDto.getPhoto())
                .gender(profileDto.getGender())
                .dateOfBirth(profileDto.getDateOfBirth())
                .createdAt(profileDto.getCreatedAt())
                .build();
    }

    @Override
    public ProfileDto toDto(ProfileEntity profileEntity) {
        return ProfileDto
                .builder()
                .id(profileEntity.getId())
                .username(profileEntity.getUsername())
                .email(profileEntity.getEmail())
                .password(profileEntity.getPassword())
                .name(profileEntity.getName())
                .aboutMe(profileEntity.getAboutMe())
                .photo(profileEntity.getPhoto())
                .gender(profileEntity.getGender())
                .dateOfBirth(profileEntity.getDateOfBirth())
                .createdAt(profileEntity.getCreatedAt())
                .build();
    }
}
