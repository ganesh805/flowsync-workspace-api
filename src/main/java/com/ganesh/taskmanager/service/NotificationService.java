package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.entity.Notification;
import com.ganesh.taskmanager.entity.User;

import com.ganesh.taskmanager.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor

public class NotificationService {

    private final NotificationRepository
            notificationRepository;

    // SEND NOTIFICATION

    public void sendNotification(

            User user,

            String message

    ) {

        Notification notification =

                Notification.builder()

                        .message(message)

                        .user(user)

                        .isRead(false)

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .build();

        notificationRepository
                .save(notification);
    }

    // GET USER NOTIFICATIONS

    public List<Notification>
    getNotifications(User user) {

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(
                        user
                );
    }

    // MARK SINGLE READ
    public void markAsRead(Long id, User user) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification Not Found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    // MARK ALL READ
    public void markAllAsRead(
            User user
    ) {

        List<Notification>
                notifications =

                notificationRepository
                        .findByUserOrderByCreatedAtDesc(
                                user
                        );

        notifications.forEach(notification ->

                notification.setRead(true)
        );

        notificationRepository
                .saveAll(notifications);
    }
}