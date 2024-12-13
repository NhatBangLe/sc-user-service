package com.microservices.user.controller;

import com.microservices.user.dto.request.DomainCreateRequest;
import com.microservices.user.dto.request.DomainUpdateRequest;
import com.microservices.user.dto.response.DomainResponse;
import com.microservices.user.dto.response.PagingObjectsResponse;
import com.microservices.user.service.IDomainService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/domain")
@RequiredArgsConstructor
@Tag(name = "Domain", description = "All endpoints about domains.")
public class DomainController {

    private final IDomainService domainService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagingObjectsResponse<DomainResponse> getAllDomains(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(required = false, defaultValue = "6") Integer pageSize
    ) {
        return domainService.getAllDomains(name, pageNumber, pageSize);
    }

    @GetMapping("/{domainId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(
            responseCode = "404",
            description = "Domain not found",
            content = @Content(
                    mediaType = MediaType.TEXT_PLAIN_VALUE,
                    schema = @Schema(example = "Domain not found")
            )
    )
    public DomainResponse getDomain(@PathVariable String domainId) {
        return domainService.getDomain(domainId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "82b29d0a-8dfe-4c6d-85ad-2a4a11e1e5d8"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid creating data.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "name cannot be blank.")
                    )
            )
    })
    public String createDomain(@RequestBody DomainCreateRequest domainCreateRequest) {
        return domainService.createDomain(domainCreateRequest);
    }

    @PatchMapping("/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid updating data.",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            schema = @Schema(example = "name cannot be blank.")
                    )
            )
    })
    public void updateDomain(@PathVariable String domainId,
                             @RequestBody DomainUpdateRequest domainUpdateRequest) {
        domainService.updateDomain(domainId, domainUpdateRequest);
    }

    @DeleteMapping("/{domainId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", content = @Content)
    public void deleteDomain(@PathVariable String domainId) {
        domainService.deleteDomain(domainId);
    }

}