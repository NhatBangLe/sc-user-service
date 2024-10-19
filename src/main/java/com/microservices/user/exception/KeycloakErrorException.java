package com.microservices.user.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class KeycloakErrorException extends RuntimeException {

    private final HttpStatusCode statusCode;
    private final String errorMessage;

    public KeycloakErrorException(String message, Throwable cause, HttpStatusCode statusCode, String errorMessage) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

}
