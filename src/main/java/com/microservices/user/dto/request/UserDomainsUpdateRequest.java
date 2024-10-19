package com.microservices.user.dto.request;

import com.microservices.user.constant.RequestOperator;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Set;

public record UserDomainsUpdateRequest(
        @NotNull(message = "operator cannot be null.")
        RequestOperator operator,
        @NotNull(message = "domainIds cannot be null.")
        Set<String> domainIds
) implements Serializable {
}
