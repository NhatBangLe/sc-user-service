package com.microservices.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record AuthenticatedResponse(
        @JsonProperty("access_token")
        String accessToken,
        @JsonProperty("expires_in")
        Long expiresIn,
        @JsonProperty("refresh_expires_in")
        Long refreshExpiresIn,
        @JsonProperty("token_type")
        String tokenType,
        @JsonProperty("not-before-policy")
        Long notBeforePolicy,
        @JsonProperty("session_state")
        String sessionState,
        String scope
) implements Serializable {
}
