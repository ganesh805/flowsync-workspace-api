package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.ResignationRequest;
import com.ganesh.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResignationRepository extends JpaRepository<ResignationRequest, Long> {

    List<ResignationRequest> findByEmployeeOrderByCreatedAtDesc(User employee);

    List<ResignationRequest> findByOrganizationOrderByCreatedAtDesc(Organization organization);

    Optional<ResignationRequest> findFirstByEmployeeAndStatusOrderByCreatedAtDesc(User employee, ResignationRequest.ResignationStatus status);
}
