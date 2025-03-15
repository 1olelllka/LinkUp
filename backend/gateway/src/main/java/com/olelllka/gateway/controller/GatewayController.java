package com.olelllka.gateway.controller;

import com.olelllka.gateway.domain.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayController {
    @RequestMapping("/fallback")
    public ResponseEntity<ErrorMessage> fallback() {
        return new ResponseEntity<>(
                ErrorMessage.builder()
                .message("Service is temporarily unavailable. Please, try again later")
                        .build(), HttpStatus.SERVICE_UNAVAILABLE);
    }
}
