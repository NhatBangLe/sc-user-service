package com.microservices.user.service;

import com.microservices.user.dto.request.UserDomainsUpdateRequest;
import com.microservices.user.dto.request.UserUpdateRequest;
import com.microservices.user.dto.response.UserResponse;
import com.microservices.user.exception.IllegalAttributeException;
import com.microservices.user.exception.NoEntityFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface IUserService {

    UserResponse getUserById(@NotBlank String userId) throws NoEntityFoundException;

    UserResponse getUserByEmail(@NotBlank String userEmail) throws NoEntityFoundException;

    void updateUser(@NotBlank String userId, @NotNull @Valid UserUpdateRequest userUpdateRequest)
            throws NoEntityFoundException, IllegalAttributeException;

    void updateUserDomains(@NotBlank String userId,
                           @NotNull @Valid UserDomainsUpdateRequest userDomainsUpdateRequest)
            throws NoEntityFoundException;

    List<UserResponse> getAllExpertsByDomain(@NotBlank String domainId);
}
