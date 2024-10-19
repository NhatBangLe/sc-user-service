package com.microservices.user.service.impl;

import com.microservices.user.config.KeycloakConfigurationProperties;
import com.microservices.user.constant.OAuth2Constants;
import com.microservices.user.dto.keycloak.ErrorMessageResponse;
import com.microservices.user.dto.keycloak.ErrorWithDescriptionResponse;
import com.microservices.user.dto.keycloak.KeycloakRegisterRequest;
import com.microservices.user.dto.response.AuthenticatedResponse;
import com.microservices.user.dto.request.UserLoginRequest;
import com.microservices.user.dto.request.UserRegisterRequest;
import com.microservices.user.entity.User;
import com.microservices.user.exception.KeycloakErrorException;
import com.microservices.user.repository.UserRepository;
import com.microservices.user.service.IKeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class KeycloakService implements IKeycloakService {

    private final String tokenUri;
    private final KeycloakConfigurationProperties keycloakProperties;
    private final RestClient keycloakClient;

    private final UserRepository userRepository;

    @Autowired
    public KeycloakService(KeycloakConfigurationProperties keycloakProperties,
                           UserRepository userRepository) {
        this.userRepository = userRepository;
        this.keycloakProperties = keycloakProperties;
        tokenUri = keycloakProperties.getServerUrl() + "/realms/"
                   + keycloakProperties.getRealm()
                   + keycloakProperties.getTokenPath();
        this.keycloakClient = RestClient.builder()
                .messageConverters(configurer -> configurer.add(new FormHttpMessageConverter()))
                .build();
    }

    public AuthenticatedResponse login(UserLoginRequest userLoginRequest) throws KeycloakErrorException {
        var body = Map.of(
                "grant_type", List.of(OAuth2Constants.PASSWORD),
                "username", List.of(userLoginRequest.username()),
                "password", List.of(userLoginRequest.password()),
                "client_id", List.of(keycloakProperties.getClientId()),
                "client_secret", List.of(keycloakProperties.getClientSecret())
        );

        try {
            return createFormUrlEncodedRequest(tokenUri, body)
                    .retrieve()
                    .toEntity(AuthenticatedResponse.class)
                    .getBody();
        } catch (HttpClientErrorException e) {
            var statusCode = e.getStatusCode();
            var errorMessage = "User is UNAUTHORIZED";

            if (statusCode.isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
                var response = e.getResponseBodyAs(ErrorWithDescriptionResponse.class);
                if (response != null) errorMessage = response.description();
            }

            throw new KeycloakErrorException(
                    e.getMessage(),
                    e.getCause(),
                    statusCode,
                    errorMessage
            );
        }
    }

    public AuthenticatedResponse refreshToken(String refreshToken) throws KeycloakErrorException {
        var body = Map.of(
                "grant_type", List.of("refresh_token"),
                "refresh_token", List.of(refreshToken),
                "client_id", List.of(keycloakProperties.getClientId()),
                "client_secret", List.of(keycloakProperties.getClientSecret())
        );

        try {
            return createFormUrlEncodedRequest(tokenUri, body)
                    .retrieve()
                    .toEntity(AuthenticatedResponse.class)
                    .getBody();
        } catch (HttpClientErrorException e) {
            var statusCode = e.getStatusCode();
            var errorMessage = "Unknown error";

            if (statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                var response = e.getResponseBodyAs(ErrorWithDescriptionResponse.class);
                if (response != null) errorMessage = response.description();
            }

            throw new KeycloakErrorException(
                    e.getMessage(),
                    e.getCause(),
                    statusCode,
                    errorMessage
            );
        }
    }

    public String register(UserRegisterRequest userRegisterRequest) throws KeycloakErrorException {
        try {
            var createResponse = registerUserWithKeycloak(userRegisterRequest);
            var locationPath = Objects.requireNonNull(createResponse.getHeaders().getLocation()).getPath();

            // save new user to database
            var userId = locationPath.substring(locationPath.lastIndexOf('/') + 1);
            var newUser = User.builder()
                    .id(userId)
                    .firstName(userRegisterRequest.firstName())
                    .lastName(userRegisterRequest.lastName())
                    .gender(userRegisterRequest.gender())
                    .birthDate(userRegisterRequest.birthdate())
                    .email(userRegisterRequest.email())
                    .build();
            userRepository.save(newUser);
            return userId;
        } catch (HttpClientErrorException e) {
            var statusCode = e.getStatusCode();
            var errorMessage = "Unknown error";

            if (statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                var response = e.getResponseBodyAs(ErrorWithDescriptionResponse.class);
                if (response != null) errorMessage = response.description();
            } else if (statusCode.isSameCodeAs(HttpStatus.CONFLICT)) {
                var response = e.getResponseBodyAs(ErrorMessageResponse.class);
                if (response != null) errorMessage = response.errorMessage();
            }

            throw new KeycloakErrorException(
                    e.getMessage(),
                    e.getCause(),
                    statusCode,
                    errorMessage
            );
        }
    }

    private ResponseEntity<?> registerUserWithKeycloak(UserRegisterRequest userRegisterRequest)
            throws HttpClientErrorException {
        var serviceAccountCredential = getServiceAccountCredential();

        var credentials = List.of(
                new KeycloakRegisterRequest.Credential(
                        "password",
                        userRegisterRequest.password(),
                        false
                )
        );
//      var clientRoles = Map.of(keycloakProperties.getClientId(), List.of("worker"));
        var registerBody = KeycloakRegisterRequest.builder()
                .username(userRegisterRequest.username())
                .email(userRegisterRequest.email())
                .emailVerified(true)
                .enabled(true)
                .credentials(credentials)
//                .clientRoles(clientRoles)
                .build();
        var uri = keycloakProperties.getServerUrl() + "/admin/realms/"
                  + keycloakProperties.getRealm() + "/users";
        return keycloakClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization",
                        serviceAccountCredential.tokenType() + " "
                        + serviceAccountCredential.accessToken())
                .body(registerBody)
                .retrieve()
                .toEntity(Object.class);
    }

    private AuthenticatedResponse getServiceAccountCredential() throws HttpClientErrorException {
        var tokenBody = Map.of(
                "grant_type", List.of(OAuth2Constants.CLIENT_CREDENTIALS),
                "client_id", List.of(keycloakProperties.getClientId()),
                "client_secret", List.of(keycloakProperties.getClientSecret())
        );
        var serviceAccountCredentialResponse =
                createFormUrlEncodedRequest(tokenUri, tokenBody)
                        .retrieve()
                        .toEntity(AuthenticatedResponse.class);
        return serviceAccountCredentialResponse.getBody();
    }

    private <K, V> RestClient.RequestBodySpec createFormUrlEncodedRequest(String uri, Map<K, List<V>> body)
            throws HttpClientErrorException {
        return keycloakClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(CollectionUtils.toMultiValueMap(body));
    }

}
