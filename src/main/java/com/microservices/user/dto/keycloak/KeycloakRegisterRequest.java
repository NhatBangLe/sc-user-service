package com.microservices.user.dto.keycloak;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class KeycloakRegisterRequest implements Serializable {

    private String username;
    private String email;
    private Boolean emailVerified;
    private Boolean enabled;
    private List<Credential> credentials;

    public record Credential(
            String type,
            String value,
            Boolean temporary
    ) implements Serializable {
    }

}
