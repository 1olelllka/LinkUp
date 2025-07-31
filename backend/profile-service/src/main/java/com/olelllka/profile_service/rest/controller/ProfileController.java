package com.olelllka.profile_service.rest.controller;

import com.olelllka.profile_service.domain.dto.*;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.mapper.impl.ProfileMapper;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileMapper profileMapper;
    private final ProfileService profileService;

    @GetMapping("")
    public ResponseEntity<Page<ListOfProfilesDto>> searchForProfiles(@RequestParam(name = "search") String search,
                                                                     Pageable pageable) {
        Page<ProfileEntity> profiles = profileService.searchForProfile(search, pageable);
        Page<ListOfProfilesDto> result = profiles.map(this::mapEntityToListDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{profile_id}")
    public ResponseEntity<ProfileDto> getProfileById(@PathVariable UUID profile_id) {
        ProfileEntity profile = profileService.getProfileById(profile_id);
        return new ResponseEntity<>(profileMapper.toDto(profile), HttpStatus.OK);
    }

    @PatchMapping("/{profile_id}")
    public ResponseEntity<ProfileDto> updateProfileById(@PathVariable UUID profile_id,
                                                        @Valid @RequestBody PatchProfileDto dto,
                                                        @RequestHeader(name="Authorization") String header,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        ProfileEntity updatedProfile = profileService.updateProfile(profile_id, dto, header.substring(7));
        return new ResponseEntity<>(profileMapper.toDto(updatedProfile), HttpStatus.OK);
    }

    @DeleteMapping("/{profile_id}")
    public ResponseEntity deleteProfileById(@PathVariable UUID profile_id,
                                            @RequestHeader(name = "Authorization") String header) {
        profileService.deleteById(profile_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/follow")
    public ResponseEntity<SuccessErrorMessage> followTheUser(@RequestBody @Valid FollowDto followDto,
                                                             BindingResult bindingResult,
                                                             @RequestHeader(name="Authorization") String header) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining( " "));
            throw new ValidationException(msg);
        }
        profileService.followNewProfile(followDto.getFollowerId(), followDto.getFolloweeId(), header.substring(7));
        return new ResponseEntity<>(SuccessErrorMessage.builder().message("User " + followDto.getFollowerId() + " successfully followed user " + followDto.getFolloweeId()).build(), HttpStatus.OK);
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<SuccessErrorMessage> unfollowTheUser(@RequestBody @Valid FollowDto followDto,
                                                               BindingResult bindingResult,
                                                               @RequestHeader(name = "Authorization") String header) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        profileService.unfollowProfile(followDto.getFollowerId(), followDto.getFolloweeId(), header.substring(7));
        return new ResponseEntity<>(SuccessErrorMessage.builder().message("User " + followDto.getFollowerId() + " successfully unfollowed user " + followDto.getFolloweeId()).build(), HttpStatus.OK);
    }

    @GetMapping("/follow-status")
    public ResponseEntity<?> checkFollowStatus(@RequestParam(name = "from") UUID from,
                                                                 @RequestParam(name = "to") UUID to) {
        boolean status = profileService.checkFollowStatus(from, to);
        if (status) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
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
}
