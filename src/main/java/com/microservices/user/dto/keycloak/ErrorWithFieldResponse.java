package com.microservices.user.dto.keycloak;

import java.io.Serializable;
import java.util.List;

public record ErrorWithFieldResponse(
        String field,
        String errorMessage,
        List<String> params
) implements Serializable {
}
