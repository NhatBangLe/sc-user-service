package com.microservices.user.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record ErrorWithDescriptionResponse(
        String error,
        @JsonProperty("error_description")
        String description
) implements Serializable {
}
