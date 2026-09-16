package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "resignation_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResignationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String letter;

    private LocalDate proposedLastDay;
    private LocalDate approvedLastDay;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResignationStatus status = ResignationStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminFeedback;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({
            "password",
            "assignedTasks",
            "createdTasks",
            "organization",
            "hibernateLazyInitializer",
            "handler"
    })
    private User employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler"
    })
    private Organization organization;

    public enum ResignationStatus {
        PENDING, APPROVED, REJECTED
    }
}
