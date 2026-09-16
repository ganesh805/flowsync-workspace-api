package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ganesh.taskmanager.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String username;

    private String designation;

    private LocalDateTime createdAt;

    private String phone;

    @Column(length = 2000)
    private String bio;

    private String skills;

    private String profileImage;

    private String employeeCode;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    @JsonIgnoreProperties({"password", "assignedTasks", "createdTasks", "team", "organization", "createdBy", "hibernateLazyInitializer", "handler"})
    private User createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id")
    @JsonIgnoreProperties({"teams", "members", "hibernateLazyInitializer", "handler"})
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties({"members", "lead", "organization", "hibernateLazyInitializer", "handler"})
    private Team team;

    @OneToMany(mappedBy = "assignedTo")
    @JsonIgnore
    private List<Task> assignedTasks;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore
    private List<Task> createdTasks;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}