package com.olelllka.stories_service.rest.controller;

import com.olelllka.stories_service.rest.exception.AuthException;
import com.olelllka.stories_service.rest.exception.NotFoundException;
import com.olelllka.stories_service.domain.dto.ErrorDto;
import com.olelllka.stories_service.rest.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> notFoundException(NotFoundException ex) {
        return new ResponseEntity<>(ErrorDto.builder().message(ex.getMessage()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDto> validationException(ValidationException ex) {
        return new ResponseEntity<>(ErrorDto.builder().message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorDto> authException(AuthException ex) {
        return new ResponseEntity<>(ErrorDto.builder().message(ex.getMessage()).build(), HttpStatus.UNAUTHORIZED);
    }

}
