package com.microservices.user.dto.keycloak;

import java.io.Serializable;

public record ErrorMessageResponse(
        String errorMessage
) implements Serializable {
}
