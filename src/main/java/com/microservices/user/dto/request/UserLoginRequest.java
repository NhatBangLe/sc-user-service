package com.microservices.user.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

public record UserLoginRequest(
        @NotBlank(message = "username cannot be blank.")
        String username,
        @NotBlank(message = "password cannot be blank.")
        String password
) implements Serializable {
}
