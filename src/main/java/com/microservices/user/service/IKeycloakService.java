package com.microservices.user.service;

import com.microservices.user.dto.request.UserLoginRequest;
import com.microservices.user.dto.request.UserRegisterRequest;
import com.microservices.user.dto.response.AuthenticatedResponse;
import com.microservices.user.exception.KeycloakErrorException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@Validated
public interface IKeycloakService {

    /**
     * Authenticate a user with username and password.
     * This method will request Keycloak server to authenticate by PASSWORD grant type
     * and receive a response to return (the action is similar a broker).
     *
     * @param userLoginRequest User authenticating data.
     * @return A response from Keycloak server.
     * @throws KeycloakErrorException RestClient exception converted to.
     */
    AuthenticatedResponse login(@NotNull @Valid UserLoginRequest userLoginRequest) throws KeycloakErrorException;

    boolean logout(@NotBlank @Size(min = 36, max = 36) String userId) throws KeycloakErrorException;

    /**
     * Refresh a user credential by using refresh token.
     * This method will request Keycloak server to refresh token
     * and receive a response to return (the action is similar a broker).
     *
     * @param refreshToken A token is used to refresh.
     * @return A response from Keycloak server.
     * @throws KeycloakErrorException RestClient exception converted to.
     */
    AuthenticatedResponse refreshToken(@NotBlank @Valid String refreshToken) throws KeycloakErrorException;

    /**
     * Register a new user.
     * This method will request Keycloak server to register a new user
     * and receive a response to return (the action is similar a broker).
     * It also saves user information data to database.
     *
     * @param isExpert Flag to determine an expert registering process.
     * @param userRegisterRequest User registering data.
     * @return A response from Keycloak server.
     * @throws KeycloakErrorException RestClient exception converted to.
     */
    String register(@NotNull Boolean isExpert, @NotNull @Valid UserRegisterRequest userRegisterRequest)
            throws KeycloakErrorException;

    boolean deleteUser(@NotBlank @Size(min = 36, max = 36) String userId) throws KeycloakErrorException;

}
