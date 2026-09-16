package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.entity.Notification;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.repository.UserRepository;
import com.ganesh.taskmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private User getUserByAuth(Authentication authentication) {
        String input = authentication.getName();
        return userRepository.findByEmailIgnoreCase(input)
                .or(() -> userRepository.findByUsernameIgnoreCase(input))
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    @GetMapping
    public List<Notification> getNotifications(Authentication authentication) {
        User user = getUserByAuth(authentication);
        return notificationService.getNotifications(user);
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id, Authentication authentication) {
        User user = getUserByAuth(authentication);
        notificationService.markAsRead(id, user);
    }

    @PutMapping("/read-all")
    public void markAllAsRead(Authentication authentication) {
        User user = getUserByAuth(authentication);
        notificationService.markAllAsRead(user);
    }
}