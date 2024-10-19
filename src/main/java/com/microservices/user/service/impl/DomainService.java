package com.microservices.user.service.impl;

import com.microservices.user.dto.request.DomainCreateRequest;
import com.microservices.user.dto.request.DomainUpdateRequest;
import com.microservices.user.dto.response.DomainResponse;
import com.microservices.user.entity.Domain;
import com.microservices.user.exception.IllegalAttributeException;
import com.microservices.user.exception.NoEntityFoundException;
import com.microservices.user.repository.DomainRepository;
import com.microservices.user.service.IDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainService implements IDomainService {

    private final DomainRepository domainRepository;

    @Override
    public DomainResponse getDomain(String domainId) throws NoEntityFoundException {
        var domain = findDomainById(domainId);
        return convertDomainToResponse(domain);
    }

    @Override
    public List<DomainResponse> getAllDomains(Integer pageNumber, Integer pageSize) {
        var pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));
        return domainRepository.findAll(pageable).stream()
                .map(this::convertDomainToResponse)
                .toList();
    }

    @Override
    public String createDomain(DomainCreateRequest domainCreateRequest) {
        var domain = Domain.builder()
                .name(domainCreateRequest.name())
                .description(domainCreateRequest.description())
                .build();
        return domainRepository.save(domain).getId();
    }

    @Override
    public void updateDomain(String domainId, DomainUpdateRequest domainUpdateRequest)
            throws NoEntityFoundException, IllegalAttributeException {
        var domain = findDomainById(domainId);
        var isUpdated = false;

        var name = domainUpdateRequest.name();
        if (name != null) {
            if (name.isBlank())
                throw new IllegalAttributeException("name cannot be blank.");
            domain.setName(name);
            isUpdated = true;
        }

        var description = domainUpdateRequest.description();
        if (description != null) {
            domain.setDescription(description);
            isUpdated = true;
        }

        if (isUpdated) domainRepository.save(domain);
    }

    @Override
    public void deleteDomain(String domainId) {
        domainRepository.deleteById(domainId);
    }

    private Domain findDomainById(String domainId) throws NoEntityFoundException {
        return domainRepository.findById(domainId)
                .orElseThrow(() -> new NoEntityFoundException("No domain found with id: " + domainId));
    }

    private DomainResponse convertDomainToResponse(Domain domain) {
        return new DomainResponse(
                domain.getId(),
                domain.getName(),
                domain.getDescription()
        );
    }

}
