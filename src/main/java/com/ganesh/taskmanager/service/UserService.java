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
import com.ganesh.taskmanager.repository.ResignationRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OrganizationRepository organizationRepository;

    private final ResignationRepository resignationRepository;

    public UserResponseDto registerUser(RegisterRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Organization organization = organizationRepository.findByCompanyCode(request.getCompanyCode())
                .orElseThrow(() -> new RuntimeException("Invalid Company Code"));

        String companyDomain = organization.getCompanyDomain().toLowerCase().trim();
        String email = request.getEmail().toLowerCase().trim();

        if (!email.endsWith("@" + companyDomain)) {
            throw new RuntimeException("Email must belong to @" + companyDomain);
        }

        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .designation(request.getDesignation())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .organization(organization)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

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
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email or Username is required");
        }

        String loginInput = request.getEmail().trim();

        User user = userRepository.findByEmailIgnoreCase(loginInput)
                .or(() -> userRepository.findByUsernameIgnoreCase(loginInput))
                .or(() -> userRepository.findByEmployeeCodeIgnoreCase(loginInput))
                .orElseThrow(() -> new RuntimeException("Invalid email, username, or password"));

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Invalid email or password");
        }

        java.util.Optional<com.ganesh.taskmanager.entity.ResignationRequest> approvedResignOpt = resignationRepository.findFirstByEmployeeAndStatusOrderByCreatedAtDesc(user, com.ganesh.taskmanager.entity.ResignationRequest.ResignationStatus.APPROVED);
        if (approvedResignOpt.isPresent()) {
            com.ganesh.taskmanager.entity.ResignationRequest req = approvedResignOpt.get();
            java.time.LocalDate lastDay = req.getApprovedLastDay() != null ? req.getApprovedLastDay() : req.getProposedLastDay();
            if (lastDay != null && java.time.LocalDate.now().isAfter(lastDay)) {
                throw new RuntimeException("Account Deactivated: Your notice period and last working day (" + lastDay + ") was completed. Access to FlowSync Workspace has expired.");
            }
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getName(),
                user.getRole().name(),
                user.getOrganization().getCompanyName()
        );
    }

    public String registerCompany(CompanyRegisterDto dto) {
        if (organizationRepository.findByCompanyCode(dto.getCompanyCode()).isPresent()) {
            throw new RuntimeException("Company Code already exists");
        }

        if (organizationRepository.findByCompanyDomain(dto.getCompanyDomain()).isPresent()) {
            throw new RuntimeException("Company Domain already exists");
        }

        if (userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Admin email already exists");
        }

        String companyDomain = dto.getCompanyDomain().trim().toLowerCase();
        String adminEmail = dto.getEmail().trim().toLowerCase();

        if (!adminEmail.endsWith("@" + companyDomain)) {
            throw new RuntimeException("Admin email must belong to @" + companyDomain);
        }

        Organization organization = Organization.builder()
                .companyName(dto.getCompanyName().trim())
                .companyCode(dto.getCompanyCode().trim().toUpperCase())
                .companyDomain(companyDomain)
                .createdAt(LocalDateTime.now())
                .build();

        Organization savedOrg = organizationRepository.save(organization);

        User admin = User.builder()
                .name(dto.getAdminName())
                .email(adminEmail)
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.OWNER)
                .organization(savedOrg)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);

        return "Company Registered Successfully";
    }

    public String changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .or(() -> userRepository.findByUsernameIgnoreCase(email))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        boolean matches = passwordEncoder.matches(currentPassword, user.getPassword());
        if (!matches) {
            throw new RuntimeException("Current Password Incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password Updated";
    }
}