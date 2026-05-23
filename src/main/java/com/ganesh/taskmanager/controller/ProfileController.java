package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.ProfileUpdateDto;

import com.ganesh.taskmanager.entity.User;

import com.ganesh.taskmanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")

@RequiredArgsConstructor

@CrossOrigin("*")

public class ProfileController {

    private final UserRepository
            userRepository;

    // =========================
    // CURRENT USER
    // =========================

    private User getCurrentUser(){

        Authentication authentication =

                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email =
                authentication.getName();

        return userRepository
                .findByEmail(email)

                .orElseThrow(() ->

                        new RuntimeException(
                                "User Not Found"
                        )
                );
    }

    // =========================
    // GET PROFILE
    // =========================

    @GetMapping

    public User getProfile(){

        return getCurrentUser();
    }

    // =========================
    // UPDATE PROFILE
    // =========================

    @PutMapping

    public User updateProfile(

            @RequestBody
            ProfileUpdateDto dto

    ){

        User user =
                getCurrentUser();

        // =========================
        // UPDATE FIELDS
        // =========================

        user.setName(
                dto.getName()
        );

        user.setDesignation(
                dto.getDesignation()
        );

        user.setPhone(
                dto.getPhone()
        );

        user.setBio(
                dto.getBio()
        );

        user.setSkills(
                dto.getSkills()
        );

        user.setProfileImage(
                dto.getProfileImage()
        );

        // =========================
        // SAVE USER
        // =========================

        return userRepository
                .save(user);
    }
}