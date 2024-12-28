package com.microservices.user.service.impl;

import com.microservices.user.config.KeycloakConfigurationProperties;
import com.microservices.user.constant.OAuth2Constants;
import com.microservices.user.dto.keycloak.ErrorMessageResponse;
import com.microservices.user.dto.keycloak.ErrorWithDescriptionResponse;
import com.microservices.user.dto.keycloak.ErrorWithFieldResponse;
import com.microservices.user.dto.keycloak.KeycloakRegisterRequest;
import com.microservices.user.dto.response.AuthenticatedResponse;
import com.microservices.user.dto.request.UserLoginRequest;
import com.microservices.user.dto.request.UserRegisterRequest;
import com.microservices.user.entity.User;
import com.microservices.user.exception.KeycloakErrorException;
import com.microservices.user.repository.UserRepository;
import com.microservices.user.service.IKeycloakService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.*;

@Slf4j
@Service
public class KeycloakService implements IKeycloakService {

    private final String tokenUrl;
    private final String adminRealmPath;
    private final KeycloakConfigurationProperties properties;
    private final RestClient client;

    private final List<String> freeCredentialPaths = new ArrayList<>();

    private final UserRepository userRepository;

    private AuthenticatedResponse credential;
    private Long credentialExpiresIn = 0L; // millisecond

    @Autowired
    public KeycloakService(
            final KeycloakConfigurationProperties properties,
            final UserRepository userRepository
    ) {
        this.userRepository = userRepository;
        this.properties = properties;
        this.tokenUrl = properties.getServerUrl() + "/realms/"
                        + properties.getRealm()
                        + "/protocol/openid-connect/token";
        this.adminRealmPath = properties.getServerUrl() + "/admin/realms/" + properties.getRealm();
        this.client = initClient();

        freeCredentialPaths.addAll(List.of("token", "logout"));
    }

    public AuthenticatedResponse login(UserLoginRequest userLoginRequest) throws KeycloakErrorException {
        var body = Map.of(
                "grant_type", List.of(OAuth2Constants.PASSWORD),
                "username", List.of(userLoginRequest.username()),
                "password", List.of(userLoginRequest.password()),
                "client_id", List.of(properties.getClientId()),
                "client_secret", List.of(properties.getClientSecret())
        );

        try {
            return createFormUrlEncodedRequest(tokenUrl, body)
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
                    e.getLocalizedMessage(),
                    e.getCause(),
                    statusCode,
                    errorMessage
            );
        }
    }

    public boolean logout(String userId) throws KeycloakErrorException {
        try {
            return client.post()
                    .uri(adminRealmPath + "/users" + userId + "/logout")
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode()
                    .isSameCodeAs(HttpStatus.NO_CONTENT);
        } catch (HttpClientErrorException e) {
            var statusCode = e.getStatusCode();
            var errorMessage = e.getLocalizedMessage();

            throw new KeycloakErrorException(
                    errorMessage,
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
                "client_id", List.of(properties.getClientId()),
                "client_secret", List.of(properties.getClientSecret())
        );

        try {
            return createFormUrlEncodedRequest(tokenUrl, body)
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
                    e.getLocalizedMessage(),
                    e.getCause(),
                    statusCode,
                    errorMessage
            );
        }
    }

    public String register(Boolean isExpert, UserRegisterRequest userRegisterRequest)
            throws KeycloakErrorException {
        var createResponse = registerUserWithKeycloak(userRegisterRequest);
        var locationPath = Objects.requireNonNull(createResponse.getHeaders().getLocation()).getPath();
        var userId = locationPath.substring(locationPath.lastIndexOf('/') + 1);

        // save new user to database
        var newUser = User.builder()
                .id(userId)
                .isExpert(isExpert)
                .firstName(userRegisterRequest.firstName())
                .lastName(userRegisterRequest.lastName())
                .gender(userRegisterRequest.gender())
                .birthDate(userRegisterRequest.birthdate())
                .email(userRegisterRequest.email())
                .build();
        userRepository.save(newUser);
        return userId;
    }

    public boolean deleteUser(String userId) throws KeycloakErrorException {
        try {
            return client.delete()
                    .uri(adminRealmPath + "/users/" + userId)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            var statusCode = e.getStatusCode();
            var errorMessage = e.getLocalizedMessage();

            throw new KeycloakErrorException(
                    errorMessage,
                    e.getCause(),
                    statusCode,
                    errorMessage
            );
        }
    }

    private ResponseEntity<Void> registerUserWithKeycloak(UserRegisterRequest userRegisterRequest)
            throws KeycloakErrorException {
        var credentials = List.of(
                new KeycloakRegisterRequest.Credential(
                        "password",
                        userRegisterRequest.password(),
                        false
                )
        );

        var registerBody = KeycloakRegisterRequest.builder()
                .username(userRegisterRequest.username())
                .email(userRegisterRequest.email())
                .emailVerified(true)
                .enabled(true)
                .credentials(credentials)
                .build();
        var uri = adminRealmPath + "/users";

        try {
            return client.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(registerBody)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            var statusCode = e.getStatusCode();
            var errorMessage = "Unknown error";

            if (statusCode.isSameCodeAs(HttpStatus.BAD_REQUEST)) {
                var isErrorFieldResponse = e.getResponseBodyAsString().contains("\"field\":");
                if (isErrorFieldResponse) {
                    var fieldResponse = e.getResponseBodyAs(ErrorWithFieldResponse.class);
                    if (fieldResponse != null) errorMessage = fieldResponse.errorMessage();
                } else {
                    var response = e.getResponseBodyAs(ErrorWithDescriptionResponse.class);
                    if (response != null) errorMessage = response.description();
                }
            } else if (statusCode.isSameCodeAs(HttpStatus.CONFLICT)) {
                var response = e.getResponseBodyAs(ErrorMessageResponse.class);
                if (response != null) errorMessage = response.errorMessage();
            }

            throw new KeycloakErrorException(
                    e.getLocalizedMessage(),
                    e.getCause(),
                    statusCode,
                    errorMessage
            );
        }
    }

    private <K, V> RestClient.RequestBodySpec createFormUrlEncodedRequest(String uri, Map<K, List<V>> body)
            throws HttpClientErrorException {
        return client.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(CollectionUtils.toMultiValueMap(body));
    }

    private AuthenticatedResponse getServiceAccountCredential() throws HttpClientErrorException {
        var tokenBody = Map.of(
                "grant_type", List.of(OAuth2Constants.CLIENT_CREDENTIALS),
                "client_id", List.of(properties.getClientId()),
                "client_secret", List.of(properties.getClientSecret())
        );

        var client = RestClient.builder()
                .messageConverters(configurer -> configurer.add(new FormHttpMessageConverter()))
                .build();
        return client.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(CollectionUtils.toMultiValueMap(tokenBody))
                .retrieve()
                .toEntity(AuthenticatedResponse.class)
                .getBody();
    }

    private RestClient initClient() {
        return RestClient.builder()
                .messageConverters(configurer -> configurer.add(new FormHttpMessageConverter()))
                .requestInterceptor((request, body, execution) -> {
                    final var path = request.getURI().getPath();
                    if (freeCredentialPaths.stream().noneMatch(path::contains)) {
                        if (credential == null || System.currentTimeMillis() > credentialExpiresIn) {
                            credential = getServiceAccountCredential();
                            credentialExpiresIn = System.currentTimeMillis() + credential.expiresIn() * 1000;
                        }
                        var token = credential.tokenType() + " " + credential.accessToken();
                        var headers = request.getHeaders();
                        headers.remove(HttpHeaders.AUTHORIZATION);
                        headers.add(HttpHeaders.AUTHORIZATION, token);
                    }

                    return execution.execute(request, body);
                })
                .build();
    }

}
