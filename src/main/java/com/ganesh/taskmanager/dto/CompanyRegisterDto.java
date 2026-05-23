package com.ganesh.taskmanager.dto;

import lombok.Data;

@Data
public class CompanyRegisterDto {

    private String companyName;

    private String companyCode;

    private String adminName;

    private String email;

    private String password;
}