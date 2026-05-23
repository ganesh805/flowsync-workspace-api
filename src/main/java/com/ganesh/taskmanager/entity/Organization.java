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
    @GeneratedValue(
            strategy =
                    GenerationType.IDENTITY
    )
    private Long id;

    @Column(unique = true)
    private String companyName;

    @Column(unique = true)
    private String companyCode;

    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "organization")

    @JsonIgnore

    private List<User> users;
}