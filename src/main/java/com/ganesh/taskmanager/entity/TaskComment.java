package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_comments")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TaskComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "task_id")

    @JsonIgnoreProperties({
            "comments"
    })

    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "user_id")

    @JsonIgnoreProperties({
            "password",
            "organization",
            "hibernateLazyInitializer",
            "handler"
    })

    private User user;
}