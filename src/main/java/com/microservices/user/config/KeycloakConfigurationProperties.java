package com.microservices.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("keycloak")
public class KeycloakConfigurationProperties {

    /**
     * Keycloak server url.
     */
    private String serverUrl;
    private String realm;
    private String clientId;
    private String clientSecret;
    private String tokenPath;

}
