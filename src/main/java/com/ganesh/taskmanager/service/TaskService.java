package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.TaskRequestDto;

import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.User;

import com.ganesh.taskmanager.enums.Status;

import com.ganesh.taskmanager.repository.TaskRepository;
import com.ganesh.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class TaskService {

    private final TaskRepository taskRepository;

    private final UserRepository userRepository;

    private final NotificationService
            notificationService;

    private final ActivityLogService
            activityLogService;

    // =========================
    // CURRENT USER
    // =========================

    private User getCurrentUser() {

        Authentication authentication =

                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email =
                authentication.getName();

        return userRepository
                .findByEmail(email)

                .orElseThrow(() ->

                        new RuntimeException(
                                "User Not Found"
                        )
                );
    }

    // =========================
    // CREATE TASK
    // =========================

    public Task createTask(

            TaskRequestDto dto,

            String email

    ) {

        User currentUser =

                userRepository
                        .findByEmail(email)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "User Not Found"
                                )
                        );

        User assignedUser =

                userRepository
                        .findById(dto.getAssignedToId())

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Assigned User Not Found"
                                )
                        );

        Task task = Task.builder()

                .title(dto.getTitle())

                .description(dto.getDescription())

                .priority(dto.getPriority())

                .dueDate(dto.getDueDate())

                .status(Status.PENDING)

                .assignedTo(assignedUser)

                .createdBy(currentUser)

                .organization(
                        currentUser.getOrganization()
                )

                .build();

        Task savedTask =
                taskRepository.save(task);

        // SEND NOTIFICATION

        notificationService.sendNotification(

                assignedUser,

                "New task assigned: "
                        + task.getTitle()
        );

        // ACTIVITY LOG

        activityLogService.save(

                "Task Assigned",

                "Task '" + task.getTitle()

                        + "' assigned to "

                        + assignedUser.getName()
        );

        return savedTask;
    }

    // =========================
    // GET TASKS
    // =========================

    public List<Task> getAllTasks() {

        User currentUser =
                getCurrentUser();

        // ADMIN

        if(currentUser.getRole()
                .name()
                .equals("ADMIN")) {

            return taskRepository.findByOrganization(

                    currentUser.getOrganization()
            );
        }

        // EMPLOYEE

        return taskRepository.findByAssignedTo(
                currentUser
        );
    }

    // =========================
    // UPDATE STATUS
    // =========================

    public Task updateTaskStatus(

            Long id,

            String status

    ) {

        User currentUser =
                getCurrentUser();

        Task task =
                taskRepository.findById(id)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Task Not Found"
                                )
                        );

        if(task.getAssignedTo() == null) {

            throw new RuntimeException(
                    "Task has no assigned employee"
            );
        }

        Status newStatus =
                Status.valueOf(status);

        // =========================
        // MEMBER RULES
        // =========================

        if(currentUser.getRole()
                .name()
                .equals("MEMBER")) {

            if(!task.getAssignedTo()
                    .getId()
                    .equals(currentUser.getId())) {

                throw new RuntimeException(
                        "Unauthorized"
                );
            }

            // MEMBER CANNOT DIRECTLY COMPLETE

            if(newStatus == Status.COMPLETED) {

                throw new RuntimeException(
                        "Admin approval required"
                );
            }
        }

        // UPDATE STATUS

        task.setStatus(newStatus);

        Task updatedTask =
                taskRepository.save(task);

        // =========================
        // APPROVAL REQUEST
        // =========================

        if(newStatus
                == Status.COMPLETION_REQUESTED) {

            List<User> admins =

                    userRepository.findByOrganization(

                                    task.getOrganization()

                            )

                            .stream()

                            .filter(user ->

                                    user.getRole()
                                            .name()
                                            .equals("ADMIN")
                            )

                            .toList();

            for(User admin : admins) {

                notificationService.sendNotification(

                        admin,

                        task.getAssignedTo()
                                .getName()

                                + " requested approval for task: "

                                + task.getTitle()
                );
            }

            // ACTIVITY LOG

            activityLogService.save(

                    "Approval Request",

                    task.getAssignedTo().getName()

                            + " requested approval for task "

                            + task.getTitle()
            );
        }

        // =========================
        // TASK APPROVED
        // =========================

        if(newStatus
                == Status.COMPLETED) {

            notificationService.sendNotification(

                    task.getAssignedTo(),

                    "Your task was approved and completed"
            );

            // ACTIVITY LOG

            activityLogService.save(

                    "Task Approved",

                    task.getTitle()
                            + " approved by admin"
            );
        }

        return updatedTask;
    }

    // =========================
    // DELETE TASK
    // =========================

    public void deleteTask(Long id) {

        Task task =
                taskRepository.findById(id)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Task Not Found"
                                )
                        );

        String taskTitle =
                task.getTitle();

        taskRepository.delete(task);

        // ACTIVITY LOG

        activityLogService.save(

                "Task Deleted",

                taskTitle
                        + " deleted successfully"
        );
    }

    // =========================
    // UPDATE TASK
    // =========================

    public Task updateTask(

            Long id,

            Task updatedTask

    ) {

        Task existingTask =

                taskRepository.findById(id)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Task Not Found"
                                )
                        );

        existingTask.setTitle(
                updatedTask.getTitle()
        );

        existingTask.setDescription(
                updatedTask.getDescription()
        );

        existingTask.setDueDate(
                updatedTask.getDueDate()
        );

        existingTask.setPriority(
                updatedTask.getPriority()
        );

        Task savedTask =
                taskRepository.save(existingTask);

        // ACTIVITY LOG

        activityLogService.save(

                "Task Updated",

                existingTask.getTitle()
                        + " updated successfully"
        );

        return savedTask;
    }

    // =========================
    // GET SINGLE TASK
    // =========================

    public Task getTaskById(
            Long id
    ) {

        return taskRepository
                .findById(id)
                .orElseThrow(() ->

                        new RuntimeException(
                                "Task Not Found"
                        )
                );
    }
}