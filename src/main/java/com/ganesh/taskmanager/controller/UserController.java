package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.AuthResponse;
import com.ganesh.taskmanager.dto.LoginRequest;
import com.ganesh.taskmanager.dto.RegisterRequest;
import com.ganesh.taskmanager.dto.UserResponseDto;
import com.ganesh.taskmanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.ganesh.taskmanager.dto.CompanyRegisterDto;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponseDto registerUser(
            @Valid @RequestBody RegisterRequest request
    ) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public AuthResponse loginUser(
            @Valid @RequestBody LoginRequest request
    ) {
        return userService.loginUser(request);
    }

    @PostMapping("/register-company")
    public String registerCompany(

            @RequestBody
            CompanyRegisterDto dto

    ) {

        return userService
                .registerCompany(dto);
    }
    @PutMapping("/change-password")
    public String changePassword(

            @RequestParam
            String currentPassword,

            @RequestParam
            String newPassword,

            Authentication authentication

    ) {

        return userService.changePassword(

                authentication.getName(),

                currentPassword,

                newPassword
        );
    }
}