package com.olelllka.chat_service.rest.controller;

import com.olelllka.chat_service.domain.dto.ErrorMessage;
import com.olelllka.chat_service.rest.exception.NotFoundException;
import com.olelllka.chat_service.rest.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AdviceController {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorMessage> notFoundException(NotFoundException ex) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .message(ex.getMessage()).build(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorMessage> validationException(ValidationException ex) {
        return new ResponseEntity<>(ErrorMessage.builder()
                .message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }

}
