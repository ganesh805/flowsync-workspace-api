package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )

    private Long id;

    private String name;

    @Column(unique = true)

    private String email;

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

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @OneToMany(mappedBy = "assignedTo")
    @JsonIgnore
    private List<Task> assignedTasks;
    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore
    private List<Task> createdTasks;
    @PrePersist

    public void prePersist(){

        createdAt =
                LocalDateTime.now();
    }
}