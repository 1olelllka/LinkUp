package com.olelllka.image_storage.controller;

import com.olelllka.image_storage.domain.ErrorDto;
import com.olelllka.image_storage.exceptions.InvalidTypeException;
import com.olelllka.image_storage.exceptions.NotFoundException;
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

    @ExceptionHandler(InvalidTypeException.class)
    public ResponseEntity<ErrorDto> invalidTypeException(InvalidTypeException ex) {
        return new ResponseEntity<>(ErrorDto.builder().message(ex.getMessage()).build(), HttpStatus.BAD_REQUEST);
    }
}
