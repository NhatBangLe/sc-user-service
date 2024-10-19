package com.microservices.user.dto.response;

import com.microservices.user.constant.Gender;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for {@link com.microservices.user.entity.User}
 */
public record UserResponse(
        String id,
        String firstName,
        String lastName,
        Gender gender,
        LocalDate birthDate,
        String email,
        List<String> domainIds
) implements Serializable {
}