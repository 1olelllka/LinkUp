package com.olelllka.auth_service.rest.controller;

import com.olelllka.auth_service.domain.dto.ErrorMessage;
import com.olelllka.auth_service.rest.exception.DuplicateException;
import com.olelllka.auth_service.rest.exception.NotFoundException;
import com.olelllka.auth_service.rest.exception.UnauthorizedException;
import com.olelllka.auth_service.rest.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorMessage> unauthorizedException(UnauthorizedException ex) {
        return new ResponseEntity<>(ErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> notFoundException(NotFoundException ex) {
        return new ResponseEntity<>(ErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorMessage> validationException(ValidationException ex) {
        return new ResponseEntity<>(ErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorMessage> duplicateException(DuplicateException ex) {
        return new ResponseEntity<>(ErrorMessage.builder().message(ex.getMessage()).build(), HttpStatus.CONFLICT);
    }
}
