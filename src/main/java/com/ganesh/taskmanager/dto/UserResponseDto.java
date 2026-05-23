package com.ganesh.taskmanager.dto;

import com.ganesh.taskmanager.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponseDto {

    private Long id;

    private String name;

    private String username;
    private String designation;

    private String email;

    private Role role;

    private LocalDateTime createdAt;
}