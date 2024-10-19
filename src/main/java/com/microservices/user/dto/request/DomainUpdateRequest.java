package com.microservices.user.dto.request;

import java.io.Serializable;

/**
 * DTO for {@link com.microservices.user.entity.Domain}
 */
public record DomainUpdateRequest(
        String name,
        String description
) implements Serializable {
}