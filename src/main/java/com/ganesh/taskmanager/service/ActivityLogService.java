package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.entity.ActivityLog;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.repository.ActivityLogRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    private User getCurrentUserOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return userRepository.findByEmail(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    // SAVE LOG
    public void save(
            String action,
            String details
    ) {
        User currentUser = getCurrentUserOrNull();

        ActivityLog log = ActivityLog.builder()
                .action(action)
                .details(details)
                .performedBy(currentUser != null ? currentUser.getName() : "SYSTEM")
                .organization(currentUser != null ? currentUser.getOrganization() : null)
                .build();

        activityLogRepository.save(log);
    }

    // GET ALL LOGS FOR CURRENT USER'S ORGANIZATION (Scoped by Team Lead)
    public List<ActivityLog> getAllLogs() {
        User currentUser = getCurrentUserOrNull();
        if (currentUser != null && currentUser.getOrganization() != null) {
            List<ActivityLog> logs = activityLogRepository.findByOrganizationOrderByCreatedAtDesc(currentUser.getOrganization());
            if (currentUser.getRole() == com.ganesh.taskmanager.enums.Role.OWNER) {
                return logs;
            }
            List<User> teamMates = userRepository.findByCreatedBy(currentUser);
            List<String> validNames = new java.util.ArrayList<>(teamMates.stream().map(User::getName).toList());
            validNames.add(currentUser.getName());

            return logs.stream()
                    .filter(l -> l.getPerformedBy() != null && validNames.contains(l.getPerformedBy()))
                    .toList();
        }
        return List.of();
    }
}