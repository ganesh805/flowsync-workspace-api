package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.CommentRequestDto;

import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.TaskComment;
import com.ganesh.taskmanager.entity.User;

import com.ganesh.taskmanager.repository.TaskCommentRepository;
import com.ganesh.taskmanager.repository.TaskRepository;
import com.ganesh.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final TaskCommentRepository
            taskCommentRepository;

    private final TaskRepository
            taskRepository;

    private final UserRepository
            userRepository;

    public TaskComment addComment(

            Long taskId,

            CommentRequestDto dto,

            String email
    ) {

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow();

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow();

        TaskComment comment =
                TaskComment.builder()

                        .message(dto.getMessage())

                        .createdAt(LocalDateTime.now())

                        .task(task)

                        .user(user)

                        .build();

        return taskCommentRepository
                .save(comment);
    }

    public List<TaskComment>
    getComments(Long taskId) {

        Task task =
                taskRepository.findById(taskId)
                        .orElseThrow();

        return taskCommentRepository
                .findByTask(task);
    }
}