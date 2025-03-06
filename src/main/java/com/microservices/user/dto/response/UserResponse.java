package com.microservices.user.dto.response;

import com.microservices.user.constant.Gender;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.microservices.user.entity.User}
 */
public record UserResponse(
        String id,
        String firstName,
        String lastName,
        Gender gender,
        String birthDate,
        String email,
        List<String> domainIds
) implements Serializable {
}