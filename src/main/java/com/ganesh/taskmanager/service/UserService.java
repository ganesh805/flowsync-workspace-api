package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.AuthResponse;
import com.ganesh.taskmanager.dto.LoginRequest;
import com.ganesh.taskmanager.dto.RegisterRequest;
import com.ganesh.taskmanager.dto.UserResponseDto;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.repository.UserRepository;
import com.ganesh.taskmanager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.ganesh.taskmanager.dto.CompanyRegisterDto;

import com.ganesh.taskmanager.entity.Organization;

import com.ganesh.taskmanager.enums.Role;

import com.ganesh.taskmanager.repository.OrganizationRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OrganizationRepository
            organizationRepository;

    public UserResponseDto registerUser(
            RegisterRequest request
    ) {

        if(userRepository
                .findByEmail(
                        request.getEmail()
                )
                .isPresent()) {

            throw new RuntimeException(
                    "Email already exists"
            );
        }

        Organization organization =

                organizationRepository

                        .findByCompanyCode(
                                request.getCompanyCode()
                        )

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Invalid Company Code"
                                )
                        );

        User user = User.builder()

                .name(request.getName())

                .username(request.getUsername())

                .designation(
                        request.getDesignation()
                )

                .email(request.getEmail())

                .password(

                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .role(request.getRole())

                .organization(organization)

                .createdAt(
                        LocalDateTime.now()
                )

                .build();

        User savedUser =
                userRepository.save(user);

        return new UserResponseDto(

                savedUser.getId(),

                savedUser.getName(),

                savedUser.getUsername(),

                savedUser.getDesignation(),

                savedUser.getEmail(),

                savedUser.getRole(),

                savedUser.getCreatedAt()
        );
    }

    public AuthResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password")
                );

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(

                token,

                user.getName(),

                user.getRole().name(),

                user.getOrganization()
                        .getCompanyName()
        );
    }
    public String registerCompany(

            CompanyRegisterDto dto

    ) {

        Organization organization =

                Organization.builder()

                        .companyName(
                                dto.getCompanyName()
                        )

                        .companyCode(
                                dto.getCompanyCode()
                        )

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .build();

        Organization savedOrg =
                organizationRepository
                        .save(organization);

        User admin = User.builder()

                .name(dto.getAdminName())

                .email(dto.getEmail())

                .password(

                        passwordEncoder.encode(
                                dto.getPassword()
                        )
                )

                .role(Role.ADMIN)

                .organization(savedOrg)

                .build();

        userRepository.save(admin);

        return "Company Registered";
    }

    public String changePassword(

            String email,

            String currentPassword,

            String newPassword

    ) {

        User user = userRepository

                .findByEmail(email)

                .orElseThrow(() ->

                        new RuntimeException(
                                "User Not Found"
                        )
                );

        boolean matches =

                passwordEncoder.matches(

                        currentPassword,

                        user.getPassword()
                );

        if(!matches) {

            throw new RuntimeException(
                    "Current Password Incorrect"
            );
        }

        user.setPassword(

                passwordEncoder.encode(
                        newPassword
                )
        );

        userRepository.save(user);

        return "Password Updated";
    }
}