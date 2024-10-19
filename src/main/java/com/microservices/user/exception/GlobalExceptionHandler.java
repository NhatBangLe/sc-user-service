package com.microservices.user.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationException(ConstraintViolationException e) {
        return handleException(e);
    }

    @ExceptionHandler(IllegalAttributeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalAttributeException(IllegalAttributeException e) {
        return handleException(e);
    }

    @ExceptionHandler(NoEntityFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoEntityFoundException(NoEntityFoundException e) {
        return handleException(e);
    }

    private String handleException(Exception e) {
        var message = e.getMessage();
        log.error(message, e.getCause());
        return message;
    }

}
