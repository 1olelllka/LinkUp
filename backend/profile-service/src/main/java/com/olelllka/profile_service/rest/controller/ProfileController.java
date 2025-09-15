package com.olelllka.profile_service.rest.controller;

import com.olelllka.profile_service.domain.dto.*;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.mapper.impl.ProfileMapper;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    @Tag(name="Profile management", description = "Read/Update/Delete endpoints for profile service")
    @Operation(summary = "Search for profiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search query executed successfully"),
    })
    @GetMapping("")
    public ResponseEntity<Page<ListOfProfilesDto>> searchForProfiles(@RequestParam(name = "search") String search,
                                                                     Pageable pageable) {
        Page<ProfileEntity> profiles = profileService.searchForProfile(search, pageable);
        Page<ListOfProfilesDto> result = profiles.map(this::mapEntityToListDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Tag(name="Profile management")
    @Operation(summary = "Get specific profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the profile"),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            })
    })
    @GetMapping("/{profile_id}")
    public ResponseEntity<ProfileDto> getProfileById(@PathVariable UUID profile_id) {
        ProfileEntity profile = profileService.getProfileById(profile_id);
        return new ResponseEntity<>(profileMapper.toDto(profile), HttpStatus.OK);
    }

    @Tag(name="Profile management")
    @Operation(summary = "Update specific profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the profile"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            }),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            }),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            })
    })
    @PatchMapping("/{profile_id}")
    public ResponseEntity<ProfileDto> updateProfileById(@PathVariable UUID profile_id,
                                                        @RequestHeader(name="Authorization") String header,
                                                        @Valid @RequestBody PatchProfileDto dto,
                                                        BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        ProfileEntity updatedProfile = profileService.updateProfile(profile_id, dto, header.substring(7));
        return new ResponseEntity<>(profileMapper.toDto(updatedProfile), HttpStatus.OK);
    }

    @Tag(name="Profile management")
    @Operation(summary = "Delete specific profile by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted the profile. The deleted ID is being transferred to other services via RabbitMQ"),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            })
    })
    @DeleteMapping("/{profile_id}")
    public ResponseEntity deleteProfileById(@PathVariable UUID profile_id,
                                            @RequestHeader(name = "Authorization") String header) {
        profileService.deleteById(profile_id, header.substring(7));
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Tag(name="Followees/Followers management", description = "CRUD endpoints for following functionalities")
    @Operation(summary = "Follow specific profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully followed the profile"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            }),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            }),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            })
    })
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

    @Tag(name="Followees/Followers management")
    @Operation(summary = "Unfollow specific profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully unfollowed the profile"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            }),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            }),
            @ApiResponse(responseCode = "404", description = "Profile not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            })
    })
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

    @Tag(name="Followees/Followers management")
    @Operation(summary = "Check following status of two profiles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile_1 follows profile_2"),
            @ApiResponse(responseCode = "404", description = "Following relation not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = SuccessErrorMessage.class))
            })
    })
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

    @Tag(name="Followees/Followers management")
    @Operation(summary = "Get all followers for the profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the followers of the profile"),
    })
    @GetMapping("/{profile_id}/followers")
    public ResponseEntity<Page<ListOfProfilesDto>> getAllFollowersForProfile(@PathVariable UUID profile_id,
                                                                             Pageable pageable) {
        Page<ProfileEntity> profiles = profileService.getFollowersById(profile_id, pageable);
        Page<ListOfProfilesDto> result = profiles.map(this::mapEntityToListDto);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Tag(name="Followees/Followers management")
    @Operation(summary = "Get all followees for the profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched the followees of the profile"),
    })
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
