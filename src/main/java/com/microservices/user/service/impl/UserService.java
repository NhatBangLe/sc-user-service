package com.microservices.user.service.impl;

import com.microservices.user.dto.request.UserDomainsUpdateRequest;
import com.microservices.user.dto.request.UserUpdateRequest;
import com.microservices.user.dto.response.UserResponse;
import com.microservices.user.entity.Domain;
import com.microservices.user.entity.User;
import com.microservices.user.exception.IllegalAttributeException;
import com.microservices.user.exception.KeycloakErrorException;
import com.microservices.user.exception.NoEntityFoundException;
import com.microservices.user.repository.DomainRepository;
import com.microservices.user.repository.UserRepository;
import com.microservices.user.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final DomainRepository domainRepository;
    private final KeycloakService keycloakService;

    @Override
    public UserResponse getUserById(String userId) throws NoEntityFoundException {
        var user = findUserById(userId);
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String userEmail) throws NoEntityFoundException {
        var user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new NoEntityFoundException("No user found with email: " + userEmail));
        return convertToUserResponse(user);
    }

    @Override
    public void updateUser(String userId, UserUpdateRequest userUpdateRequest)
            throws NoEntityFoundException, IllegalAttributeException {
        var user = findUserById(userId);
        var isUpdated = false;

        var firstName = userUpdateRequest.firstName();
        if (firstName != null) {
            if (firstName.isBlank())
                throw new IllegalAttributeException("firstName cannot be blank.");
            user.setFirstName(firstName);
            isUpdated = true;
        }

        var lastName = userUpdateRequest.lastName();
        if (lastName != null) {
            user.setLastName(lastName);
            isUpdated = true;
        }

        var gender = userUpdateRequest.gender();
        if (gender != null) {
            user.setGender(gender);
            isUpdated = true;
        }

        var birthDate = userUpdateRequest.birthDate();
        if (birthDate != null) {
            user.setBirthDate(birthDate);
            isUpdated = true;
        }

        if (isUpdated) userRepository.save(user);
    }

    @Override
    public void updateUserDomains(String userId, UserDomainsUpdateRequest userDomainsUpdateRequest)
            throws NoEntityFoundException, IllegalAttributeException {
        var user = findUserById(userId);
        var isUpdated = false;

        var domainIds = userDomainsUpdateRequest.domainIds();
        if (domainIds != null) {
            var currentDomain = user.getDomains();
            var currentDomainIds = currentDomain.stream().map(Domain::getId).toList();
            var validDomainIds = userDomainsUpdateRequest.domainIds().stream()
                    .filter(id -> !currentDomainIds.contains(id))
                    .toList();
            if (validDomainIds.isEmpty()) return;

            var newDomains = domainRepository.findAllById(validDomainIds);

            currentDomain.addAll(newDomains);
            user.setDomains(currentDomain);
            isUpdated = true;
        }

        if (isUpdated) userRepository.save(user);
    }

    @Override
    public List<UserResponse> getAllExpertsByDomain(String domainId) {
        var domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new NoEntityFoundException("No entity found with id: " + domainId));
        return domain.getUsers().stream()
                .filter(User::getIsExpert)
                .map(this::convertToUserResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackOn = {KeycloakErrorException.class})
    public void deleteUser(String userId) throws NoEntityFoundException {
        var user = findUserById(userId);
        userRepository.delete(user);
        var successful = keycloakService.deleteUser(userId);
        if (successful) log.debug("Successfully deleted user: {}", userId);
    }

    private User findUserById(String userId) throws NoEntityFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NoEntityFoundException("No user found with id " + userId));
    }

    private UserResponse convertToUserResponse(User user) {
        var domainIds = user.getDomains().stream()
                .map(Domain::getId)
                .toList();

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getGender(),
                user.getBirthDate(),
                user.getEmail(),
                domainIds
        );
    }

}
