package com.microservices.user.dto.request;

import com.microservices.user.constant.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.LocalDate;

public record UserRegisterRequest(
        @NotBlank(message = "username cannot be blank.")
        String username,
        @NotBlank(message = "password cannot be blank.")
        String password,
        @NotBlank(message = "firstName cannot be blank.")
        String firstName,
        @NotNull(message = "lastName cannot be null.")
        String lastName,
        @NotBlank(message = "email cannot be blank.")
        String email,
        @NotNull(message = "gender cannot be null.")
        Gender gender,
        @NotNull(message = "birthdate cannot be null.")
        LocalDate birthdate
) implements Serializable {
}
