package com.olelllka.notification_service.rest.controller;

import com.olelllka.notification_service.domain.dto.ErrorMessage;
import com.olelllka.notification_service.rest.exception.AuthException;
import com.olelllka.notification_service.rest.exception.ForbiddenException;
import com.olelllka.notification_service.rest.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> notFoundException(NotFoundException ex) {
        return new ResponseEntity<>(ErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorMessage> authException(AuthException ex) {
        return new ResponseEntity<>(ErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorMessage> forbiddenException(ForbiddenException ex) {
        return new ResponseEntity<>(ErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.FORBIDDEN);
    }

}
