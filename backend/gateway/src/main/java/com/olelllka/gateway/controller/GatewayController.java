package com.olelllka.gateway.controller;

import com.olelllka.gateway.domain.ErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name="Gateway API Endpoint")
public class GatewayController {

    @Operation(summary = "Fallback for circuit breaker for every HTTP method")
    @ApiResponse(responseCode = "503", description = "Service Unavailable", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))
    })
    @RequestMapping("/fallback")
    public ResponseEntity<ErrorMessage> fallback() {
        return new ResponseEntity<>(
                ErrorMessage.builder()
                .message("Service is temporarily unavailable. Please, try again later")
                        .build(), HttpStatus.SERVICE_UNAVAILABLE);
    }
}
