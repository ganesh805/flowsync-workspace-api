package com.ganesh.taskmanager.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String companyName;

    @Column(nullable = false, unique = true)
    private String companyCode;

    @Column(nullable = false)
    private String companyDomain;

    @Column(columnDefinition = "TEXT")
    private String logoUrl;

    @Column(length = 1000)
    private String description;

    private String industry;

    private String contactEmail;

    private String contactPhone;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "organization")
    @JsonIgnore
    private List<User> users;

    @OneToMany(mappedBy = "organization")
    @JsonIgnore
    private List<Team> teams;
}