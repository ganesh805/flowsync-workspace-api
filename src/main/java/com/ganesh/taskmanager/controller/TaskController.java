package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.TaskRequestDto;
import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor

public class TaskController {

    private final TaskService taskService;
    // CREATE TASK

    @PostMapping

    public Task createTask(

            @RequestBody TaskRequestDto dto,

            Authentication authentication

    ) {

        return taskService.createTask(

                dto,

                authentication.getName()
        );
    }
    // GET TASKS
    @GetMapping
    public List<Task> getTasks() {

        return taskService.getAllTasks();
    }

    // UPDATE STATUS

    @PutMapping("/{id}/status")

    public Task updateTaskStatus(

            @PathVariable Long id,

            @RequestParam String status

    ) {

        return taskService.updateTaskStatus(
                id,
                status
        );
    }

    // DELETE TASK

    @DeleteMapping("/{id}")

    public void deleteTask(

            @PathVariable Long id

    ) {

        taskService.deleteTask(id);
    }

    // UPDATE TASK

    @PutMapping("/{id}")

    public Task updateTask(

            @PathVariable Long id,

            @RequestBody Task task

    ) {

        return taskService.updateTask(
                id,
                task
        );
    }
    // GET SINGLE TASK
    @GetMapping("/{id}")

    public Task getTaskById(

            @PathVariable Long id

    ) {

        return taskService.getTaskById(id);
    }
}