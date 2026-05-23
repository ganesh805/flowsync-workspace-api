package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.CommentRequestDto;

import com.ganesh.taskmanager.entity.TaskComment;

import com.ganesh.taskmanager.service.TaskCommentService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class TaskCommentController {

    private final TaskCommentService
            taskCommentService;

    @PostMapping("/{taskId}")

    public TaskComment addComment(

            @PathVariable Long taskId,

            @RequestBody CommentRequestDto dto,

            Authentication authentication
    ) {

        return taskCommentService.addComment(

                taskId,

                dto,

                authentication.getName()
        );
    }

    @GetMapping("/{taskId}")

    public List<TaskComment>
    getComments(

            @PathVariable Long taskId
    ) {

        return taskCommentService
                .getComments(taskId);
    }
}