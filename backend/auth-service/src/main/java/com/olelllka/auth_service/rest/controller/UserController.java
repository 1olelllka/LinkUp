package com.olelllka.auth_service.rest.controller;

import com.olelllka.auth_service.domain.dto.*;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.rest.exception.ValidationException;
import com.olelllka.auth_service.service.JWTUtil;
import com.olelllka.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerNewUser(@Valid @RequestBody RegisterUserDto userDto,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        UserEntity registeredUser = userService.registerUser(userDto);
        return new ResponseEntity<>(mapToDto(registeredUser), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JWTToken> loginUser(@Valid @RequestBody LoginUser loginUser,
                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginUser.getEmail(),
                loginUser.getPassword()
        ));
        if (authentication.isAuthenticated()) {
            String jwt = userService.generateJWTViaEmail(loginUser.getEmail());
            return new ResponseEntity<>(JWTToken.builder().token(jwt).build(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyUser(@RequestHeader(name = "Authorization") String header) {
        UserEntity user = userService.getUserByJwt(header.substring(7));
        return new ResponseEntity<>(mapToDto(user), HttpStatus.OK);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDto> patchUser(@RequestHeader(name = "Authorization") String header,
                                            @Valid @RequestBody PatchUserDto patchUserDto,
                                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(" "));
            throw new ValidationException(msg);
        }
        UserEntity patched = userService.patchUser(header.substring(7), patchUserDto);
        return new ResponseEntity<>(mapToDto(patched), HttpStatus.OK);
    }

    private UserDto mapToDto(UserEntity entity) {
        return UserDto.builder()
                .userId(entity.getUserId())
                .role(entity.getRole())
                .email(entity.getEmail())
                .alias(entity.getAlias())
                .password(entity.getPassword())
                .authProvider(entity.getAuthProvider())
                .providerId(entity.getProviderId())
                .build();
    }
}
