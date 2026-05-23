package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByOrganization(
            Organization organization
    );
}