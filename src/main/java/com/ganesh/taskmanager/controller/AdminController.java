package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.EmployeePerformanceDto;

import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.User;

import com.ganesh.taskmanager.service.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor

@PreAuthorize("hasRole('ADMIN')")

public class AdminController {

    private final AdminService adminService;

    // USERS

    @GetMapping("/users")

    public List<User> getUsers() {

        return adminService.getAllUsers();
    }

    // TASKS

    @GetMapping("/tasks")

    public List<Task> getTasks() {

        return adminService.getAllTasks();
    }

    // PROMOTE USER

    @PutMapping("/users/{id}/promote")

    public User promoteUser(

            @PathVariable Long id

    ) {

        return adminService.promoteUser(id);
    }

    // DEMOTE USER

    @PutMapping("/users/{id}/demote")

    public User demoteUser(

            @PathVariable Long id

    ) {

        return adminService.demoteUser(id);
    }

    // DELETE USER

    @DeleteMapping("/users/{id}")

    public void deleteUser(

            @PathVariable Long id

    ) {

        adminService.deleteUser(id);
    }

    // EMPLOYEE PERFORMANCE

    @GetMapping("/performance")

    public List<EmployeePerformanceDto>
    getEmployeePerformance() {

        return adminService
                .getEmployeePerformance();
    }

    // UPDATE EMPLOYEE

    @PutMapping("/users/{id}")

    public User updateEmployee(

            @PathVariable Long id,

            @RequestBody User user

    ) {

        return adminService
                .updateEmployee(id, user);
    }

    // CREATE EMPLOYEE

    @PostMapping("/create-employee")

    public User createEmployee(

            @RequestBody User employee,

            Authentication authentication

    ) {

        return adminService.createEmployee(

                employee,

                authentication.getName()
        );
    }

    // UPDATE ROLE

    @PutMapping("/users/{id}/role")

    public User updateRole(

            @PathVariable Long id,

            @RequestParam String role

    ) {

        return adminService
                .updateUserRole(id, role);
    }
}