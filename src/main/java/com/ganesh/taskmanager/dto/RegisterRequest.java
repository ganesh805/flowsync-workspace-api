package com.ganesh.taskmanager.dto;

import com.ganesh.taskmanager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String name;

    @Email
    private String email;

    @NotBlank
    private String password;

    private Role role;
    @NotBlank
    private String username;
    @NotBlank
    private String designation;
    private String companyCode;
}