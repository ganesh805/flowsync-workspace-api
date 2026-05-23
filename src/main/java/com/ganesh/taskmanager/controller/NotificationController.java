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
public class NotificationController {

    private final NotificationService notificationService;

    private final UserRepository userRepository;

    @GetMapping
    public List<Notification> getNotifications(
            Authentication authentication
    ) {

        User user = userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow();

        return notificationService
                .getNotifications(user);
    }
}