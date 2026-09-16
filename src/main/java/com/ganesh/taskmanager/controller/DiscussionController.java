package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.DiscussionMessageDto;
import com.ganesh.taskmanager.entity.DiscussionMessage;
import com.ganesh.taskmanager.service.DiscussionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @PostMapping
    public DiscussionMessage sendMessage(@RequestBody DiscussionMessageDto dto) {
        return discussionService.sendMessage(
                dto.getMessage(),
                dto.getTaggedUser(),
                dto.getChannel()
        );
    }

    @GetMapping
    public List<DiscussionMessage> getMessages(@RequestParam(required = false, defaultValue = "ORGANIZATION") String channel) {
        return discussionService.getMessages(channel);
    }

    @PostMapping("/direct")
    public DiscussionMessage sendDirectMessage(@RequestBody Map<String, Object> body) {
        Long recipientId = Long.valueOf(body.get("recipientId").toString());
        String message = body.get("message").toString();
        return discussionService.sendDirectMessage(recipientId, message);
    }

    @GetMapping("/direct/{recipientId}")
    public List<DiscussionMessage> getDirectMessages(@PathVariable Long recipientId) {
        return discussionService.getDirectMessages(recipientId);
    }

    @GetMapping("/colleagues")
    public List<com.ganesh.taskmanager.entity.User> getCompanyColleagues() {
        return discussionService.getCompanyColleagues();
    }
}