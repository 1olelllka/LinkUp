package com.olelllka.profile_service.rest.controller;

import com.olelllka.profile_service.domain.dto.SuccessErrorMessage;
import com.olelllka.profile_service.rest.exception.AuthException;
import com.olelllka.profile_service.rest.exception.NotFoundException;
import com.olelllka.profile_service.rest.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviceController {
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<SuccessErrorMessage> validationException(ValidationException ex) {
        return new ResponseEntity<>(SuccessErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<SuccessErrorMessage> notFoundException(NotFoundException ex) {
        return new ResponseEntity<>(SuccessErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<SuccessErrorMessage> authException(AuthException ex) {
        return new ResponseEntity<>(SuccessErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.UNAUTHORIZED);
    }
}
