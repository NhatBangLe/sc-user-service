package com.microservices.user.controller;

import com.microservices.user.dto.request.UserDomainsUpdateRequest;
import com.microservices.user.dto.request.UserUpdateRequest;
import com.microservices.user.dto.response.UserResponse;
import com.microservices.user.service.IUserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Information")
public class UserController {

    private final IUserService userService;

    @GetMapping("/{domainId}/experts")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(
            responseCode = "404",
            description = "Domain not found",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(example = "Domain not found")
            )
    )
    public List<UserResponse> getAllExpertsByDomain(@PathVariable String domainId) {
        return userService.getAllExpertsByDomain(domainId);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(example = "User not found")
            )
    )
    public UserResponse getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @GetMapping("/{userEmail}/email")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(
            responseCode = "404",
            description = "User not found",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(example = "User not found")
            )
    )
    public UserResponse getUserByEmail(@PathVariable String userEmail) {
        return userService.getUserByEmail(userEmail);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "firstName is blank.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "firstName cannot be blank.")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "User not found")
                    )
            )
    })
    public void updateUser(@PathVariable String userId,
                           @RequestBody UserUpdateRequest userUpdateRequest) {
        userService.updateUser(userId, userUpdateRequest);
    }

    @PatchMapping("/{userId}/domain")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "operator or domainIds may be null.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "operator cannot be null.")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "User not found")
                    )
            )
    })
    public void updateUserDomains(@PathVariable String userId,
                                  @RequestBody UserDomainsUpdateRequest userDomainsUpdateRequest) {
        userService.updateUserDomains(userId, userDomainsUpdateRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", content = @Content)
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

}
