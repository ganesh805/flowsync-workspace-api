package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.ChannelDto;
import com.ganesh.taskmanager.dto.ChannelMessageDto;
import com.ganesh.taskmanager.dto.CreateChannelRequestDto;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.service.ChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @GetMapping
    public List<ChannelDto> getChannels() {
        return channelService.getChannelsForCurrentUser();
    }

    @PostMapping
    public ChannelDto createChannel(@Valid @RequestBody CreateChannelRequestDto dto) {
        return channelService.createChannel(dto);
    }

    @GetMapping("/{channelId}")
    public ChannelDto getChannelById(@PathVariable Long channelId) {
        return channelService.getChannelById(channelId);
    }

    @GetMapping("/{channelId}/messages")
    public Page<ChannelMessageDto> getChannelMessages(
            @PathVariable Long channelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return channelService.getChannelMessages(channelId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @PostMapping("/{channelId}/messages")
    public ChannelMessageDto sendChannelMessage(
            @PathVariable Long channelId,
            @RequestBody Map<String, String> body
    ) {
        String content = body.get("content");
        if (content == null) {
            content = body.get("message");
        }
        return channelService.sendChannelMessage(channelId, content);
    }

    @GetMapping("/{channelId}/members")
    public List<User> getChannelMembers(@PathVariable Long channelId) {
        return channelService.getChannelMembers(channelId);
    }

    @PostMapping("/{channelId}/members")
    public void addChannelMember(
            @PathVariable Long channelId,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = Long.valueOf(body.get("userId").toString());
        channelService.addChannelMember(channelId, userId);
    }

    @DeleteMapping("/{channelId}/members/{userId}")
    public void removeChannelMember(
            @PathVariable Long channelId,
            @PathVariable Long userId
    ) {
        channelService.removeChannelMember(channelId, userId);
    }

    @PutMapping("/messages/{messageId}")
    public ChannelMessageDto editMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body
    ) {
        String content = body.get("content");
        if (content == null) {
            content = body.get("message");
        }
        return channelService.editMessage(messageId, content);
    }

    @DeleteMapping("/messages/{messageId}")
    public void deleteMessage(@PathVariable Long messageId) {
        channelService.deleteMessage(messageId);
    }
}
