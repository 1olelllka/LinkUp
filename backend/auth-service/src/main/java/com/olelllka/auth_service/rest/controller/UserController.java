package com.olelllka.auth_service.rest.controller;

import com.olelllka.auth_service.domain.dto.JWTToken;
import com.olelllka.auth_service.domain.dto.LoginUser;
import com.olelllka.auth_service.domain.dto.RegisterUserDto;
import com.olelllka.auth_service.domain.dto.UserDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            String jwt = jwtUtil.generateJWT(loginUser.getEmail());
            return new ResponseEntity<>(JWTToken.builder().token(jwt).build(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    private UserDto mapToDto(UserEntity entity) {
        return UserDto.builder()
                .userId(entity.getUserId())
                .role(entity.getRole())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .authProvider(entity.getAuthProvider())
                .providerId(entity.getProviderId())
                .build();
    }
}
