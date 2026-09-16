package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ganesh.taskmanager.enums.MessageType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String message;
    private String taggedUser;

    @Builder.Default
    private String channel = "ORGANIZATION";

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime editedAt;
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "channel_id")
    @JsonIgnoreProperties({
            "organization",
            "team",
            "createdBy",
            "hibernateLazyInitializer",
            "handler"
    })
    private Channel channelEntity;

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
    @JoinColumn(name = "recipient_id")
    @JsonIgnoreProperties({
            "password",
            "organization",
            "hibernateLazyInitializer",
            "handler"
    })
    private User recipient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler"
    })
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties({
            "members",
            "organization",
            "hibernateLazyInitializer",
            "handler"
    })
    private Team team;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.messageType == null) {
            this.messageType = MessageType.TEXT;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}