package com.microservices.user.controller;

import com.microservices.user.dto.response.AuthenticatedResponse;
import com.microservices.user.dto.request.UserLoginRequest;
import com.microservices.user.dto.request.UserRegisterRequest;
import com.microservices.user.exception.KeycloakErrorException;
import com.microservices.user.service.impl.KeycloakService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user/auth")
@Tag(name = "Authenticator")
@RequiredArgsConstructor
public class AuthController {

    private final KeycloakService keycloakService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses({
            @ApiResponse(responseCode = "400",
                    description = "Bad request.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "Error details")
                    )
            ),
            @ApiResponse(responseCode = "401",
                    description = "Invalid credentials.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "Invalid user credentials")
                    )
            )
    })
    public AuthenticatedResponse login(@RequestBody UserLoginRequest userLoginRequest) {
        return keycloakService.login(userLoginRequest);
    }

    @PostMapping("/{userId}/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean logout(@PathVariable String userId) {
        return keycloakService.logout(userId);
    }

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(
            responseCode = "400",
            description = "Token is not active or Invalid refresh token.",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(example = "Invalid refresh token")
            )
    )
    public AuthenticatedResponse refresh(@RequestParam String refreshToken) {
        return keycloakService.refreshToken(refreshToken);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registering data.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "Invalid registering data")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email existed.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "User exists with same email")
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Identity server unavailable.",
                    content = @Content
            )
    })
    public String register(
            @RequestParam(required = false, defaultValue = "false") Boolean isExpert,
            @RequestBody UserRegisterRequest userRegisterRequest
    ) {
        return keycloakService.register(isExpert, userRegisterRequest);
    }

    @ExceptionHandler(KeycloakErrorException.class)
    public ResponseEntity<String> handleKeycloakErrorException(KeycloakErrorException exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(exception.getStatusCode())
                .body(exception.getErrorMessage());
    }

}
