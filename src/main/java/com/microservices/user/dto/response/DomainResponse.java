package com.microservices.user.dto.response;

import java.io.Serializable;

/**
 * DTO for {@link com.microservices.user.entity.Domain}
 */
public record DomainResponse(
        String id,
        String name,
        String description
) implements Serializable {
}