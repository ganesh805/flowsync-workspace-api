package com.ganesh.taskmanager.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ActivityLog {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    private String action;

    private String details;

    private String performedBy;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {

        createdAt =
                LocalDateTime.now();
    }
}