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

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Organization organization =
                organizationRepository
                        .findByCompanyCode(request.getCompanyCode())
                        .orElseThrow(() ->
                                new RuntimeException("Invalid Company Code")
                        );

        String companyDomain = organization
                .getCompanyDomain()
                .toLowerCase()
                .trim();

        String email = request.getEmail()
                .toLowerCase()
                .trim();

        if (!email.endsWith("@" + companyDomain)) {
            throw new RuntimeException(
                    "Email must belong to @" + companyDomain
            );
        }

        User user = User.builder()

                .name(request.getName())

                .username(request.getUsername())

                .designation(request.getDesignation())

                .email(email)

                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .role(request.getRole())

                .organization(organization)

                .createdAt(LocalDateTime.now())

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

        System.out.println("========== LOGIN REQUEST ==========");
        System.out.println("Email Received : " + request.getEmail());
        System.out.println("Password Entered : " + request.getPassword());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    System.out.println("User not found in database");
                    return new RuntimeException("Invalid email or password");
                });

        System.out.println("User Found");
        System.out.println("Database Email : " + user.getEmail());
        System.out.println("Encoded Password : " + user.getPassword());

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        System.out.println("Password Matches : " + matches);

        if (!matches) {
            System.out.println("Password verification failed");
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        System.out.println("JWT Generated Successfully");
        System.out.println("==============================");

        return new AuthResponse(
                token,
                user.getName(),
                user.getRole().name(),
                user.getOrganization().getCompanyName()
        );
    }
    public String registerCompany(

            CompanyRegisterDto dto

    ) {

        if (organizationRepository.findByCompanyCode(dto.getCompanyCode()).isPresent()) {
            throw new RuntimeException("Company Code already exists");
        }

        if (organizationRepository.findByCompanyDomain(dto.getCompanyDomain()).isPresent()) {
            throw new RuntimeException("Company Domain already exists");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Admin email already exists");
        }

        String companyDomain = dto.getCompanyDomain()
                .trim()
                .toLowerCase();

        String adminEmail = dto.getEmail()
                .trim()
                .toLowerCase();

        if (!adminEmail.endsWith("@" + companyDomain)) {
            throw new RuntimeException(
                    "Admin email must belong to @" + companyDomain
            );
        }

        Organization organization = Organization.builder()

                .companyName(
                        dto.getCompanyName().trim()
                )

                .companyCode(
                        dto.getCompanyCode().trim().toUpperCase()
                )

                .companyDomain(
                        companyDomain
                )

                .createdAt(
                        LocalDateTime.now()
                )

                .build();

        Organization savedOrg =
                organizationRepository.save(organization);

        User admin = User.builder()

                .name(dto.getAdminName())

                .email(adminEmail)

                .password(
                        passwordEncoder.encode(
                                dto.getPassword()
                        )
                )

                .role(Role.ADMIN)

                .organization(savedOrg)

                .createdAt(LocalDateTime.now())

                .build();

        userRepository.save(admin);

        return "Company Registered Successfully";
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