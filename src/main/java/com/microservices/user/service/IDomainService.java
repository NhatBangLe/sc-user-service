package com.microservices.user.service;

import com.microservices.user.dto.request.DomainCreateRequest;
import com.microservices.user.dto.request.DomainUpdateRequest;
import com.microservices.user.dto.response.DomainResponse;
import com.microservices.user.dto.response.PagingObjectsResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
public interface IDomainService {

    DomainResponse getDomain(String domainId);

    PagingObjectsResponse<DomainResponse> getAllDomains(
            @NotNull @Min(value = 0, message = "pageNumber cannot be less than 0.")
            Integer pageNumber,
            @NotNull @Min(value = 0, message = "pageSize cannot be less than 0.")
            Integer pageSize
    );

    String createDomain(@NotNull @Valid DomainCreateRequest domainCreateRequest);

    void updateDomain(@NotNull String domainId, @NotNull DomainUpdateRequest domainUpdateRequest);

    void deleteDomain(@NotNull String domainId);

}
