package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "discussion_messages")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscussionMessage {

    @Id
    @GeneratedValue(
            strategy =
                    GenerationType.IDENTITY
    )
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;
    private String taggedUser;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "sender_id")

    @JsonIgnoreProperties({
            "password",
            "organization",
            "hibernateLazyInitializer",
            "handler"
    })

    private User sender;

    @ManyToOne(fetch = FetchType.EAGER)

    @JoinColumn(name = "organization_id")

    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler"
    })

    private Organization organization;
}