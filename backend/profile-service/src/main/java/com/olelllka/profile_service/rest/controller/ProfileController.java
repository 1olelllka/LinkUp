package com.olelllka.profile_service.rest.controller;

import com.olelllka.profile_service.domain.dto.CreateProfileDto;
import com.olelllka.profile_service.domain.dto.ProfileDto;
import com.olelllka.profile_service.domain.entity.ProfileEntity;
import com.olelllka.profile_service.mapper.impl.ProfileMapper;
import com.olelllka.profile_service.rest.exception.ValidationException;
import com.olelllka.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/profiles")
public class ProfileController {

    @Autowired
    private ProfileMapper profileMapper;
    @Autowired
    private ProfileService profileService;

    @PostMapping("")
    public ResponseEntity<ProfileDto> createNewProfile(@Valid  @RequestBody CreateProfileDto dto,
                                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        ProfileEntity entity = profileService.createProfile(mapCreateToEntity(dto));
        return new ResponseEntity<>(profileMapper.toDto(entity), HttpStatus.CREATED);
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

}
