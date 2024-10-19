package com.microservices.user.exception;

public class IllegalAttributeException extends RuntimeException{

    public IllegalAttributeException(String message) {
        super(message);
    }

}
