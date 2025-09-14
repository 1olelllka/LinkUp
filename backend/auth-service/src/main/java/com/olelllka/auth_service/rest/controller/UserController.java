package com.olelllka.auth_service.rest.controller;

import com.olelllka.auth_service.domain.dto.*;
import com.olelllka.auth_service.domain.entity.UserEntity;
import com.olelllka.auth_service.rest.exception.ValidationException;
import com.olelllka.auth_service.service.AuthService;
import com.olelllka.auth_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    @Tag(name="Authentication/authorization Endpoints", description = "Endpoints used for register, log in, log out and extending user's session")
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registered new user. The information about created user will be sent to profile service via RabbitMQ"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "409", description = "User already exists", content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Authentication failed", content = {
                    @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ErrorMessage.class))
            })
    })
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

    @Tag(name="Authentication/authorization Endpoints")
    @Operation(summary = "Login existing user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "403", description = "Login failed", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
    })
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

    @Tag(name="Authentication/authorization Endpoints")
    @Operation(summary = "Logout user")
    @ApiResponse(responseCode = "200")
    @PostMapping("/logout")
    public void logout() {}

    @Tag(name="Authentication/authorization Endpoints")
    @Operation(summary = "Redirection to OAuth2 Google Login/Register")
    @ApiResponse(responseCode = "200")
    @GetMapping("/oauth2/authorization/google")
    public void oauth2() {}

    @Tag(name="Authentication/authorization Endpoints")
    @Operation(summary = "Update session for user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "JWT token successfully refreshed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized to perform JWT refresh", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @PostMapping("/refresh")
    public ResponseEntity<JWTTokenResponse> refreshToken(@CookieValue(name = "refresh_token") String refreshToken,
                                                         HttpServletResponse response) {
        Optional<JWTTokenResponse> tokens = authService.refreshToken(refreshToken);
        if (tokens.isPresent()) {
            ResponseCookie cookie = ResponseCookie.from("refresh_token", tokens.get().getRefreshToken())
                    .sameSite("Strict")
                    .maxAge(3600 * 24)
                    .httpOnly(true)
                    .path("/")
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return new ResponseEntity<>(tokens.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Tag(name="User's auth information", description = "Endpoints to get/update user's auth information")
    @Operation(summary = "Get auth information about user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully fetched information"),
            @ApiResponse(responseCode = "404", description = "User not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "401", description = "JWT token invalid", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyUser(@RequestHeader(name = "Authorization") String header) {
        UserEntity user = userService.getUserByJwt(header.substring(7));
        return new ResponseEntity<>(mapToDto(user), HttpStatus.OK);
    }

    @Tag(name="User's auth information")
    @Operation(summary = "Update user's auth information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully. The updated information will be sent to profile service via RabbitMQ"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "401", description = "Authorization error", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            }),
            @ApiResponse(responseCode = "404", description = "User not found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
            })
    })
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
