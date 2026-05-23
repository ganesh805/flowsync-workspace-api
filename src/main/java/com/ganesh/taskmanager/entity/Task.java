package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.ganesh.taskmanager.enums.Priority;
import com.ganesh.taskmanager.enums.Status;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tasks")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Task {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )

    private Long id;

    private String title;

    @Column(length = 2000)

    private String description;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)

    private Priority priority;

    @Enumerated(EnumType.STRING)

    private Status status;

    // CREATED BY

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "created_by")

    @JsonIgnoreProperties({
            "password",
            "organization",
            "assignedTasks",
            "createdTasks",
            "hibernateLazyInitializer",
            "handler"
    })

    private User createdBy;

    // ASSIGNED USER

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "assigned_to")

    @JsonIgnoreProperties({
            "password",
            "organization",
            "assignedTasks",
            "createdTasks",
            "hibernateLazyInitializer",
            "handler"
    })

    private User assignedTo;

    // ORGANIZATION

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "organization_id")

    @JsonIgnore

    private Organization organization;

    // COMMENTS

    @OneToMany(
            mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )

    @JsonIgnore

    private List<TaskComment> comments;
}