package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmployeeCodeIgnoreCase(String employeeCode);

    List<User> findByOrganization(Organization organization);

    long countByOrganization(Organization organization);

    List<User> findByOrganizationAndRole(Organization organization, Role role);

    long countByCreatedBy(User createdBy);

    List<User> findByCreatedBy(User createdBy);
}