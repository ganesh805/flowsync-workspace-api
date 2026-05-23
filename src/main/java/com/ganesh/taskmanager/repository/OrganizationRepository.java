package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository
        extends JpaRepository<Organization, Long> {

    Optional<Organization>
    findByCompanyCode(String companyCode);
}