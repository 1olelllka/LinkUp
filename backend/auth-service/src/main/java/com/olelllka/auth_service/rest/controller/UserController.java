package com.olelllka.auth_service.rest.controller;

import com.olelllka.auth_service.domain.dto.*;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.rest.exception.ValidationException;
import com.olelllka.auth_service.service.AuthService;
import com.olelllka.auth_service.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

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
    public ResponseEntity<JWTTokenResponse> loginUser(@Valid @RequestBody LoginUser loginUser,
                                                      HttpServletResponse servletResponse,
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
            JWTTokenResponse response = userService.generateJWTViaEmail(loginUser.getEmail());
            ResponseCookie cookie = ResponseCookie.from("refresh_token", response.getRefreshToken())
                    .maxAge(3600 * 24)
                    .sameSite("Strict")
                    .httpOnly(true)
                    .path("/")
//                    .secure(true)
                    .build();
            servletResponse.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JWTTokenResponse> refreshToken(@CookieValue(name = "refresh_token") String refreshToken) {
        Optional<String> token = authService.refreshToken(refreshToken);
        return token.map(s -> new ResponseEntity<>(JWTTokenResponse.builder().accessToken(s).build(), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
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
