package com.microservices.user.exception;

public class NoEntityFoundException extends RuntimeException {

    public NoEntityFoundException(String message) {
        super(message);
    }

}
