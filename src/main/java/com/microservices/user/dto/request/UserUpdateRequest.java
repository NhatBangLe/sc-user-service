package com.microservices.user.dto.request;

import com.microservices.user.constant.Gender;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.microservices.user.entity.User}
 */
public record UserUpdateRequest(
        String firstName,
        String lastName,
        Gender gender,
        LocalDate birthDate
) implements Serializable {
}