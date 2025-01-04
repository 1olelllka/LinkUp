package com.olelllka.profile_service.rest.controller;

import com.olelllka.profile_service.domain.dto.*;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.mapper.impl.ProfileMapper;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/profiles")
public class ProfileController {

    @Autowired
    private ProfileMapper profileMapper;
    @Autowired
    private ProfileService profileService;

    @PostMapping("")
    public ResponseEntity<ProfileDto> createNewProfile(@Valid @RequestBody CreateProfileDto dto,
                                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        ProfileEntity entity = profileService.createProfile(mapCreateToEntity(dto));
        return new ResponseEntity<>(profileMapper.toDto(entity), HttpStatus.CREATED);
    }

    @GetMapping("/{profile_id}")
    public ResponseEntity<ProfileDto> getProfileById(@PathVariable UUID profile_id) {
        ProfileEntity profile = profileService.getProfileById(profile_id);
        return new ResponseEntity<>(profileMapper.toDto(profile), HttpStatus.OK);
    }

    @PatchMapping("/{profile_id}")
    public ResponseEntity<ProfileDto> updateProfileById(@PathVariable UUID profile_id,
                                                        @Valid @RequestBody PatchProfileDto dto,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        ProfileEntity updatedProfile = profileService.updateProfile(profile_id, dto);
        return new ResponseEntity<>(profileMapper.toDto(updatedProfile), HttpStatus.OK);
    }

    @DeleteMapping("/{profile_id}")
    public ResponseEntity deleteProfileById(@PathVariable UUID profile_id) {
        profileService.deleteById(profile_id);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{profile_id}/follow")
    public ResponseEntity<SuccessErrorMessage> followTheUser(@PathVariable UUID profile_id,
                                                             @RequestParam(name = "user") UUID follow_id) {
        profileService.followNewProfile(profile_id, follow_id);
        return new ResponseEntity<>(SuccessErrorMessage.builder().message("User " + profile_id + " successfully followed user " + follow_id).build(), HttpStatus.OK);
    }

    @DeleteMapping("/{profile_id}/unfollow")
    public ResponseEntity<SuccessErrorMessage> unfollowTheUser(@PathVariable UUID profile_id,
                                                               @RequestParam(name = "user") UUID follow_id) {
        profileService.unfollowProfile(profile_id, follow_id);
        return new ResponseEntity<>(SuccessErrorMessage.builder().message("User " + profile_id + " successfully unfollowed user " + follow_id).build(), HttpStatus.OK);
    }

    @GetMapping("/{profile_id}/followers")
    public ResponseEntity<Page<ListOfProfilesDto>> getAllFollowersForProfile(@PathVariable UUID profile_id,
                                                                             Pageable pageable) {
        Page<ProfileEntity> profiles = profileService.getFollowersById(profile_id, pageable);
        Page<ListOfProfilesDto> result = profiles.map(this::mapEntityToListDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{profile_id}/followees")
    public ResponseEntity<Page<ListOfProfilesDto>> getAllFolloweesForProfile(@PathVariable UUID profile_id,
                                                                             Pageable pageable) {
        Page<ProfileEntity> profiles = profileService.getFolloweesById(profile_id, pageable);
        Page<ListOfProfilesDto> result = profiles.map(this::mapEntityToListDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    private ListOfProfilesDto mapEntityToListDto(ProfileEntity entity) {
        return ListOfProfilesDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .name(entity.getName())
                .photo(entity.getPhoto())
                .build();
    }

    private ProfileEntity mapCreateToEntity(CreateProfileDto dto) {
        return ProfileEntity.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .gender(dto.getGender())
                .name(dto.getName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getDateOfBirth())
                .build();
    }

    private ProfileEntity mapPatchToEntity(PatchProfileDto dto) {
        return ProfileEntity.builder()
                .username(dto.getUsername())
                .photo(dto.getPhoto())
                .aboutMe(dto.getAboutMe())
                .gender(dto.getGender())
                .name(dto.getName())
                .email(dto.getEmail())
                .dateOfBirth(dto.getDateOfBirth())
                .build();
    }

}
