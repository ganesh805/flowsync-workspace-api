package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.EmployeePerformanceDto;
import com.ganesh.taskmanager.dto.TaskRequestDto;

import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.User;

import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.enums.Status;

import com.ganesh.taskmanager.repository.TaskRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository
            userRepository;

    private final TaskRepository
            taskRepository;

    private final NotificationService
            notificationService;
    private final PasswordEncoder passwordEncoder;

    // GET ALL USERS

    public List<User> getAllUsers() {

        User currentAdmin =

                userRepository
                        .findByEmail(

                                SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getName()
                        )

                        .orElseThrow();

        return userRepository.findByOrganization(

                currentAdmin.getOrganization()
        );
    }

    // GET ALL TASKS

    public List<Task> getAllTasks() {

        User currentAdmin =

                userRepository
                        .findByEmail(

                                SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getName()
                        )

                        .orElseThrow();

        return taskRepository.findByOrganization(

                currentAdmin.getOrganization()
        );
    }

    // ASSIGN TASK

    public Task assignTask(

            TaskRequestDto dto,

            Long userId

    ) {

        User employee =

                userRepository.findById(userId)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Employee Not Found"
                                )
                        );

        Task task = Task.builder()

                .title(dto.getTitle())

                .description(dto.getDescription())

                .dueDate(dto.getDueDate())

                .priority(dto.getPriority())

                .status(Status.PENDING)

                .assignedTo(employee)

                .build();

        Task savedTask =
                taskRepository.save(task);

        // SEND NOTIFICATION

        notificationService.sendNotification(

                employee,

                "New task assigned: "
                        + task.getTitle()
        );

        return savedTask;
    }

    // PROMOTE USER

    public User promoteUser(Long id) {

        User user =
                userRepository.findById(id)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "User Not Found"
                                )
                        );

        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }

    // DEMOTE USER

    public User demoteUser(Long id) {

        User user =
                userRepository.findById(id)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "User Not Found"
                                )
                        );

        user.setRole(Role.MEMBER);

        return userRepository.save(user);
    }

    // DELETE USER

    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }

    // EMPLOYEE PERFORMANCE

    public List<EmployeePerformanceDto>
    getEmployeePerformance() {

        User currentAdmin =

                userRepository
                        .findByEmail(

                                SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getName()
                        )

                        .orElseThrow();

        List<User> users =

                userRepository.findByOrganization(

                        currentAdmin.getOrganization()
                );

        List<EmployeePerformanceDto> analytics =
                new ArrayList<>();

        for(User user : users) {

            List<Task> tasks =

                    taskRepository.findByOrganization(

                                    currentAdmin.getOrganization()
                            )

                            .stream()

                            .filter(task ->

                                    task.getAssignedTo() != null

                                            &&

                                            task.getAssignedTo()
                                                    .getId()

                                                    .equals(user.getId())
                            )

                            .toList();

            int assigned =
                    tasks.size();

            int completed =

                    (int) tasks.stream()

                            .filter(task ->

                                    task.getStatus()
                                            == Status.COMPLETED
                            )

                            .count();

            int pending =
                    assigned - completed;

            double completionRate =

                    assigned == 0

                            ? 0

                            : ((double) completed
                            / assigned) * 100;

            analytics.add(

                    new EmployeePerformanceDto(

                            user.getName(),

                            user.getDesignation(),

                            assigned,

                            completed,

                            pending,

                            completionRate
                    )
            );
        }

        return analytics;
    }
    public User updateEmployee(

            Long id,

            User updatedUser

    ) {

        User user = userRepository
                .findById(id)

                .orElseThrow(() ->

                        new RuntimeException(
                                "User Not Found"
                        )
                );

        user.setName(
                updatedUser.getName()
        );

        user.setUsername(
                updatedUser.getUsername()
        );

        user.setEmail(
                updatedUser.getEmail()
        );

        user.setDesignation(
                updatedUser.getDesignation()
        );

        return userRepository.save(user);
    }
    public User createEmployee(

            User employee,

            String adminEmail

    ) {

        User admin = userRepository

                .findByEmail(adminEmail)

                .orElseThrow();

        employee.setPassword(

                passwordEncoder.encode(
                        employee.getPassword()
                )
        );

        employee.setRole(Role.MEMBER);

        employee.setOrganization(
                admin.getOrganization()
        );

        employee.setCreatedAt(
                LocalDateTime.now()
        );

        return userRepository.save(employee);
    }
    private User getCurrentUser() {

        Authentication authentication =

                SecurityContextHolder

                        .getContext()

                        .getAuthentication();

        String email =
                authentication.getName();

        return userRepository
                .findByEmail(email)

                .orElseThrow(

                        () -> new RuntimeException(
                                "User Not Found"
                        )
                );
    }
    public User updateUserRole(

            Long id,

            String role

    ) {

        User user =
                userRepository.findById(id)
                        .orElseThrow();

        user.setRole(
                Role.valueOf(role)
        );

        return userRepository.save(user);
    }
}