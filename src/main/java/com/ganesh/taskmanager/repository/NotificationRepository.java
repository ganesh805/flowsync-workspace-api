package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Notification;
import com.ganesh.taskmanager.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
    findByUserOrderByCreatedAtDesc(
            User user
    );
}