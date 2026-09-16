package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.ChannelMessageDto;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.service.DirectMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/direct-messages")
@RequiredArgsConstructor
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    @GetMapping("/contacts")
    public List<User> getDirectContacts() {
        return directMessageService.getDirectContacts();
    }

    @GetMapping("/{recipientId}")
    public Page<ChannelMessageDto> getDirectMessages(
            @PathVariable Long recipientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return directMessageService.getDirectMessages(recipientId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @PostMapping("/{recipientId}")
    public ChannelMessageDto sendDirectMessage(
            @PathVariable Long recipientId,
            @RequestBody Map<String, String> body
    ) {
        String content = body.get("content");
        if (content == null) {
            content = body.get("message");
        }
        return directMessageService.sendDirectMessage(recipientId, content);
    }
}
