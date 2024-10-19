package com.microservices.user.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * DTO for {@link com.microservices.user.entity.Domain}
 */
public record DomainCreateRequest(
        @NotBlank(message = "name cannot be blank.")
        String name,
        String description
) implements Serializable {
}