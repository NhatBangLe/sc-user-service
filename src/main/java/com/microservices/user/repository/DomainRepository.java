package com.microservices.user.repository;

import com.microservices.user.entity.Domain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, String> {

    Page<Domain> findAllByNameContainsIgnoreCase(String name, Pageable pageable);

}