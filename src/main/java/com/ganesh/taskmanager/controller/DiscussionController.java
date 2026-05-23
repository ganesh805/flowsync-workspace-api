package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.DiscussionMessageDto;
import com.ganesh.taskmanager.entity.DiscussionMessage;
import com.ganesh.taskmanager.service.DiscussionService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService
            discussionService;

    @PostMapping
    public DiscussionMessage sendMessage(

            @RequestBody DiscussionMessageDto dto

    ) {

        return discussionService
                .sendMessage(
                        dto.getMessage()
                );
    }

    @GetMapping
    public List<DiscussionMessage>
    getMessages() {

        return discussionService
                .getMessages();
    }
}