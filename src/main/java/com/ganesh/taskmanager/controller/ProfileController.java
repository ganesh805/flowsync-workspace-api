package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.ProfileUpdateDto;
import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.Team;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.repository.TeamRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ProfileController {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmailIgnoreCase(email)
                .or(() -> userRepository.findByUsernameIgnoreCase(email))
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        // Self-heal missing team link for Team Leads and Employees
        if (user.getTeam() == null && user.getOrganization() != null) {
            final Long currentUserId = user.getId();
            Organization org = user.getOrganization();
            List<Team> orgTeams = teamRepository.findByOrganization(org);

            if (user.getRole() == Role.ADMIN) {
                Team team = orgTeams.stream()
                        .filter(t -> t.getLead() != null && t.getLead().getId().equals(currentUserId))
                        .findFirst()
                        .orElse(null);

                if (team == null && user.getDesignation() != null && !user.getDesignation().trim().isEmpty() && !user.getDesignation().equalsIgnoreCase("Team Lead")) {
                    String deptName = user.getDesignation().trim();
                    team = orgTeams.stream()
                            .filter(t -> t.getName().equalsIgnoreCase(deptName))
                            .findFirst()
                            .orElse(null);

                    if (team == null) {
                        team = Team.builder()
                                .name(deptName)
                                .description(deptName + " Department managed by " + user.getName())
                                .lead(user)
                                .organization(org)
                                .build();
                    } else {
                        team.setLead(user);
                    }
                    team = teamRepository.save(team);
                }

                if (team != null) {
                    user.setTeam(team);
                    user = userRepository.save(user);
                }
            } else if (user.getRole() == Role.MEMBER && user.getCreatedBy() != null) {
                Long creatorId = user.getCreatedBy().getId();
                Team team = orgTeams.stream()
                        .filter(t -> t.getLead() != null && t.getLead().getId().equals(creatorId))
                        .findFirst()
                        .orElse(null);

                if (team != null) {
                    user.setTeam(team);
                    user = userRepository.save(user);
                }
            }
        }

        return user;
    }

    @GetMapping
    public User getProfile() {
        return getCurrentUser();
    }

    @PutMapping
    public User updateProfile(@RequestBody ProfileUpdateDto dto) {
        User user = getCurrentUser();

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName().trim());
        }
        if (dto.getDesignation() != null && !dto.getDesignation().trim().isEmpty()) {
            user.setDesignation(dto.getDesignation().trim());
        }
        user.setPhone(dto.getPhone());
        user.setBio(dto.getBio());
        user.setSkills(dto.getSkills());
        user.setProfileImage(dto.getProfileImage());

        return userRepository.save(user);
    }
}