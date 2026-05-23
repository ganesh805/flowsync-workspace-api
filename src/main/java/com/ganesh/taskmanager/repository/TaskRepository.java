package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository
        extends JpaRepository<Task, Long> {

    List<Task> findByAssignedTo(User user);

    List<Task> findByOrganization(
            Organization organization
    );
}