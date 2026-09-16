package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.EmployeePerformanceDto;
import com.ganesh.taskmanager.dto.TaskRequestDto;

import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.User;

import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.enums.Status;

import com.ganesh.taskmanager.repository.TaskRepository;
import com.ganesh.taskmanager.repository.TeamRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository
            userRepository;

    private final TaskRepository
            taskRepository;

    private final NotificationService
            notificationService;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    // GET ALL USERS

    // GET ALL USERS (Strictly scoped by Team Lead / Admin)
    public List<User> getAllUsers() {
        User currentAdmin = getCurrentUser();

        if (currentAdmin.getRole() == Role.OWNER) {
            return userRepository.findByOrganization(currentAdmin.getOrganization());
        }

        return userRepository.findByOrganization(currentAdmin.getOrganization()).stream()
                .filter(u -> u.getId().equals(currentAdmin.getId()) ||
                        (u.getCreatedBy() != null && u.getCreatedBy().getId().equals(currentAdmin.getId())))
                .toList();
    }

    // GET ALL TASKS (Strictly scoped by Team Lead / Admin)
    public List<Task> getAllTasks() {
        User currentAdmin = getCurrentUser();

        if (currentAdmin.getRole() == Role.OWNER) {
            return taskRepository.findByOrganization(currentAdmin.getOrganization());
        }

        return taskRepository.findByOrganization(currentAdmin.getOrganization()).stream()
                .filter(t -> (t.getCreatedBy() != null && t.getCreatedBy().getId().equals(currentAdmin.getId())) ||
                        (t.getAssignedTo() != null && (t.getAssignedTo().getId().equals(currentAdmin.getId()) ||
                                (t.getAssignedTo().getCreatedBy() != null && t.getAssignedTo().getCreatedBy().getId().equals(currentAdmin.getId())))))
                .toList();
    }

    // ASSIGN TASK

    public Task assignTask(

            TaskRequestDto dto,

            Long userId

    ) {

        User employee =

                userRepository.findById(userId)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "Employee Not Found"
                                )
                        );

        Task task = Task.builder()

                .title(dto.getTitle())

                .description(dto.getDescription())

                .dueDate(dto.getDueDate())

                .priority(dto.getPriority())

                .status(Status.PENDING)

                .assignedTo(employee)

                .build();

        Task savedTask =
                taskRepository.save(task);

        // SEND NOTIFICATION

        notificationService.sendNotification(

                employee,

                "New task assigned: "
                        + task.getTitle()
        );

        return savedTask;
    }

    // PROMOTE USER

    public User promoteUser(Long id) {

        User user =
                userRepository.findById(id)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "User Not Found"
                                )
                        );

        user.setRole(Role.ADMIN);

        return userRepository.save(user);
    }

    // DEMOTE USER

    public User demoteUser(Long id) {

        User user =
                userRepository.findById(id)

                        .orElseThrow(() ->

                                new RuntimeException(
                                        "User Not Found"
                                )
                        );

        user.setRole(Role.MEMBER);

        return userRepository.save(user);
    }

    // DELETE USER

    public void deleteUser(Long id) {

        userRepository.deleteById(id);
    }

    // EMPLOYEE PERFORMANCE

    public List<EmployeePerformanceDto>
    getEmployeePerformance() {

        User currentAdmin =

                userRepository
                        .findByEmail(

                                SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getName()
                        )

                        .orElseThrow();

        List<User> users =

                userRepository.findByOrganization(

                        currentAdmin.getOrganization()
                );

        List<EmployeePerformanceDto> analytics =
                new ArrayList<>();

        for(User user : users) {

            List<Task> tasks =

                    taskRepository.findByOrganization(

                                    currentAdmin.getOrganization()
                            )

                            .stream()

                            .filter(task ->

                                    task.getAssignedTo() != null

                                            &&

                                            task.getAssignedTo()
                                                    .getId()

                                                    .equals(user.getId())
                            )

                            .toList();

            int assigned =
                    tasks.size();

            int completed =

                    (int) tasks.stream()

                            .filter(task ->

                                    task.getStatus()
                                            == Status.COMPLETED
                            )

                            .count();

            int pending =
                    assigned - completed;

            double completionRate =

                    assigned == 0

                            ? 0

                            : ((double) completed
                            / assigned) * 100;

            analytics.add(

                    new EmployeePerformanceDto(

                            user.getName(),

                            user.getDesignation(),

                            assigned,

                            completed,

                            pending,

                            completionRate
                    )
            );
        }

        return analytics;
    }
    public User updateEmployee(

            Long id,

            User updatedUser

    ) {

        User user = userRepository
                .findById(id)

                .orElseThrow(() ->

                        new RuntimeException(
                                "User Not Found"
                        )
                );

        String companyDomain = user.getOrganization()
                .getCompanyDomain()
                .toLowerCase()
                .trim();

        if (updatedUser.getEmail() == null
                || !updatedUser.getEmail()
                .toLowerCase()
                .endsWith("@" + companyDomain)) {

            throw new RuntimeException(
                    "Employee email must belong to @" + companyDomain
            );
        }

        user.setName(
                updatedUser.getName()
        );

        user.setUsername(
                updatedUser.getUsername()
        );
        user.setEmail(
                updatedUser.getEmail().toLowerCase().trim()
        );
        user.setDesignation(
                updatedUser.getDesignation()
        );
        return userRepository.save(user);
    }
    public User createEmployee(
            User employee,
            String adminEmail
    ) {
        User creator = userRepository.findByEmailIgnoreCase(adminEmail).orElseThrow();
        String companyDomain = creator.getOrganization().getCompanyDomain().toLowerCase().trim();

        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Employee email is required.");
        }

        String rawEmail = employee.getEmail().trim().toLowerCase();
        String cleanEmail = rawEmail;

        if (!rawEmail.contains("@")) {
            cleanEmail = rawEmail + "@" + companyDomain;
        }

        if (userRepository.findByEmailIgnoreCase(cleanEmail).isPresent()) {
            throw new RuntimeException("An account with email (" + cleanEmail + ") already exists.");
        }

        if (creator.getRole() == Role.ADMIN) {
            long currentCount = userRepository.countByCreatedBy(creator);
            if (currentCount >= 20) {
                throw new RuntimeException("Employee limit reached: A Team Lead can create a maximum of 20 team employees.");
            }
        }

        // Handle unique username
        String baseUsername = (employee.getUsername() != null && !employee.getUsername().trim().isEmpty())
                ? employee.getUsername().trim().toLowerCase()
                : cleanEmail.split("@")[0];
        String username = baseUsername;
        int userSuffix = 1;
        while (userRepository.findByUsernameIgnoreCase(username).isPresent()) {
            username = baseUsername + "_" + userSuffix++;
        }

        // Resolve creator's team
        com.ganesh.taskmanager.entity.Team creatorTeam = creator.getTeam();
        if (creatorTeam == null) {
            final User finalCreator = creator;
            creatorTeam = teamRepository.findByOrganization(creator.getOrganization()).stream()
                    .filter(t -> t.getLead() != null && t.getLead().getId().equals(finalCreator.getId()))
                    .findFirst()
                    .orElse(null);

            if (creatorTeam == null && creator.getDesignation() != null && !creator.getDesignation().trim().isEmpty()) {
                String deptName = creator.getDesignation().trim();
                creatorTeam = teamRepository.findByOrganization(creator.getOrganization()).stream()
                        .filter(t -> t.getName().equalsIgnoreCase(deptName))
                        .findFirst()
                        .orElse(null);

                if (creatorTeam == null) {
                    creatorTeam = com.ganesh.taskmanager.entity.Team.builder()
                            .name(deptName)
                            .description(deptName + " Department managed by " + creator.getName())
                            .lead(creator)
                            .organization(creator.getOrganization())
                            .build();
                    creatorTeam = teamRepository.save(creatorTeam);
                }
            }

            if (creatorTeam != null) {
                creator.setTeam(creatorTeam);
                creator = userRepository.save(creator);
            }
        }

        // Department-based employee code generation (e.g., DT-VIS001)
        String deptForCode = creatorTeam != null ? creatorTeam.getName() :
                (creator.getDesignation() != null ? creator.getDesignation() : "Development");
        String empCode = generateDeptEmployeeCode(creator.getOrganization(), deptForCode);

        // Fail-safe raw password handling
        String rawPassword = (employee.getPassword() != null && !employee.getPassword().trim().isEmpty())
                ? employee.getPassword().trim()
                : "Welcome@123";

        employee.setName(employee.getName() != null ? employee.getName().trim() : "Employee");
        employee.setUsername(username);
        employee.setPassword(passwordEncoder.encode(rawPassword));
        employee.setEmail(cleanEmail);
        employee.setEmployeeCode(empCode);
        employee.setCreatedBy(creator);
        if (employee.getRole() == null) {
            employee.setRole(Role.MEMBER);
        }
        employee.setOrganization(creator.getOrganization());
        if (creatorTeam != null) {
            employee.setTeam(creatorTeam);
        }
        employee.setCreatedAt(LocalDateTime.now());

        return userRepository.save(employee);
    }

    private String generateDeptEmployeeCode(com.ganesh.taskmanager.entity.Organization org, String deptName) {
        String companyName = org.getCompanyName().replaceAll("[^a-zA-Z]", "").toUpperCase();
        String compPrefix = companyName.length() >= 3 ? companyName.substring(0, 3) : (companyName + "EMP").substring(0, 3);

        String deptPrefix = "DT";
        if (deptName != null && !deptName.trim().isEmpty()) {
            String clean = deptName.replaceAll("[^a-zA-Z ]", "").trim();
            String[] parts = clean.split("\\s+");
            if (parts.length >= 2) {
                deptPrefix = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
            } else if (clean.length() >= 2) {
                deptPrefix = (clean.substring(0, 1) + clean.substring(clean.length() - 1)).toUpperCase();
            } else {
                deptPrefix = clean.toUpperCase() + "T";
            }
        }

        long codeSeq = userRepository.countByOrganization(org) + 1;
        String empCode = String.format("%s-%s%03d", deptPrefix, compPrefix, codeSeq);

        List<User> existingUsers = userRepository.findByOrganization(org);
        int attempts = 1;
        while (isCodeTaken(existingUsers, empCode)) {
            long randNum = 100 + java.util.concurrent.ThreadLocalRandom.current().nextInt(900);
            empCode = String.format("%s-%s%03d", deptPrefix, compPrefix, randNum);
            attempts++;
            if (attempts > 20) break;
        }

        return empCode;
    }

    private boolean isCodeTaken(List<User> users, String code) {
        return users.stream().anyMatch(u -> code.equals(u.getEmployeeCode()));
    }
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loginInput = authentication.getName();

        return userRepository.findByEmailIgnoreCase(loginInput)
                .or(() -> userRepository.findByUsernameIgnoreCase(loginInput))
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }
    public User updateUserRole(

            Long id,

            String role

    ) {

        User user =
                userRepository.findById(id)
                        .orElseThrow();

        user.setRole(
                Role.valueOf(role)
        );

        return userRepository.save(user);
    }

    public User updateUserDesignation(Long id, String designation) {
        User user = userRepository.findById(id).orElseThrow();
        user.setDesignation(designation);
        return userRepository.save(user);
    }
}