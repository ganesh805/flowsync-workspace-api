package com.ganesh.taskmanager.dto;

import lombok.*;

@Getter
@Setter

@NoArgsConstructor
@AllArgsConstructor

public class ProfileUpdateDto {

    private String name;

    private String designation;

    private String phone;

    private String bio;

    private String skills;

    private String profileImage;
}