package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.OrganizationDto;
import com.ganesh.taskmanager.dto.OrganizationMetricsDto;
import com.ganesh.taskmanager.dto.TeamDto;
import com.ganesh.taskmanager.dto.TeamLeadDto;
import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.Team;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.enums.Status;
import com.ganesh.taskmanager.repository.OrganizationRepository;
import com.ganesh.taskmanager.repository.TaskRepository;
import com.ganesh.taskmanager.repository.TeamRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import com.ganesh.taskmanager.enums.Role;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final TaskRepository taskRepository;
    private final TeamRepository teamRepository;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public OrganizationDto getOrganization() {
        User user = getCurrentUser();
        Organization org = user.getOrganization();
        return mapToDto(org);
    }

    public OrganizationDto updateOrganization(OrganizationDto dto) {
        User user = getCurrentUser();
        Organization org = user.getOrganization();

        if (dto.getCompanyName() != null && !dto.getCompanyName().trim().isEmpty()) {
            org.setCompanyName(dto.getCompanyName().trim());
        }
        org.setDescription(dto.getDescription());
        org.setIndustry(dto.getIndustry());
        org.setContactEmail(dto.getContactEmail());
        org.setContactPhone(dto.getContactPhone());
        org.setUpdatedAt(LocalDateTime.now());

        Organization saved = organizationRepository.save(org);
        return mapToDto(saved);
    }

    public OrganizationDto uploadLogo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Image file is required.");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") &&
                !contentType.equals("image/jpeg") &&
                !contentType.equals("image/jpg") &&
                !contentType.equals("image/webp"))) {
            throw new RuntimeException("Invalid file type. Only PNG, JPEG, and WebP images are allowed.");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds 5MB limit.");
        }

        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String logoDataUrl = "data:" + contentType + ";base64," + base64Image;

        User user = getCurrentUser();
        Organization org = user.getOrganization();
        org.setLogoUrl(logoDataUrl);
        org.setUpdatedAt(LocalDateTime.now());

        Organization saved = organizationRepository.save(org);
        return mapToDto(saved);
    }

    public OrganizationDto removeLogo() {
        User user = getCurrentUser();
        Organization org = user.getOrganization();
        org.setLogoUrl(null);
        org.setUpdatedAt(LocalDateTime.now());
        Organization saved = organizationRepository.save(org);
        return mapToDto(saved);
    }

    public OrganizationMetricsDto getMetrics() {
        User user = getCurrentUser();
        Organization org = user.getOrganization();

        List<User> employees = userRepository.findByOrganization(org);
        List<Team> teams = teamRepository.findByOrganization(org);
        List<Task> tasks = taskRepository.findByOrganization(org);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> t.getStatus() == Status.COMPLETED).count();
        long pendingTasks = tasks.stream().filter(t -> t.getStatus() == Status.PENDING).count();
        long inProgressTasks = tasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS || t.getStatus() == Status.COMPLETION_REQUESTED).count();

        double completionRate = totalTasks == 0 ? 0.0 : ((double) completedTasks / totalTasks) * 100.0;

        return OrganizationMetricsDto.builder()
                .totalEmployees(employees.size())
                .totalTeams(teams.size())
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .inProgressTasks(inProgressTasks)
                .completionRate(Math.round(completionRate * 10.0) / 10.0)
                .build();
    }

    public List<TeamDto> getTeams() {
        User user = getCurrentUser();
        Organization org = user.getOrganization();
        List<Team> teams = teamRepository.findByOrganization(org);
        List<Task> allTasks = taskRepository.findByOrganization(org);
        List<User> allUsers = userRepository.findByOrganization(org);

        return teams.stream().map(t -> {
            long memberCount = allUsers.stream().filter(u ->
                (u.getTeam() != null && u.getTeam().getId().equals(t.getId())) ||
                (t.getLead() != null && u.getCreatedBy() != null && u.getCreatedBy().getId().equals(t.getLead().getId()) && u.getRole() == Role.MEMBER)
            ).count();

            List<Task> teamTasks = allTasks.stream()
                    .filter(task -> {
                        if (task.getAssignedTo() == null) return false;
                        User assignee = task.getAssignedTo();
                        return (assignee.getTeam() != null && assignee.getTeam().getId().equals(t.getId())) ||
                               (t.getLead() != null && assignee.getCreatedBy() != null && assignee.getCreatedBy().getId().equals(t.getLead().getId()));
                    })
                    .toList();
            long total = teamTasks.size();
            long completed = teamTasks.stream().filter(task -> task.getStatus() == Status.COMPLETED).count();
            long pending = total - completed;
            double rate = total == 0 ? 0.0 : ((double) completed / total) * 100.0;

            return TeamDto.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .description(t.getDescription())
                    .leadId(t.getLead() != null ? t.getLead().getId() : null)
                    .leadName(t.getLead() != null ? t.getLead().getName() : "Unassigned")
                    .memberCount(memberCount)
                    .activeTasks(total)
                    .completedTasks(completed)
                    .pendingTasks(pending)
                    .completionRate(Math.round(rate * 10.0) / 10.0)
                    .build();
        }).toList();
    }

    public TeamDto createTeam(TeamDto dto) {
        User user = getCurrentUser();
        Organization org = user.getOrganization();

        User lead = null;
        if (dto.getLeadId() != null) {
            lead = userRepository.findById(dto.getLeadId()).orElse(null);
        }

        Team team = Team.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .lead(lead)
                .organization(org)
                .build();

        Team saved = teamRepository.save(team);
        return TeamDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .leadId(saved.getLead() != null ? saved.getLead().getId() : null)
                .leadName(saved.getLead() != null ? saved.getLead().getName() : "Unassigned")
                .memberCount(0)
                .activeTasks(0)
                .completedTasks(0)
                .pendingTasks(0)
                .completionRate(0.0)
                .build();
    }

    public void deleteTeam(Long teamId) {
        User user = getCurrentUser();
        Organization org = user.getOrganization();
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (!team.getOrganization().getId().equals(org.getId())) {
            throw new RuntimeException("Unauthorized team deletion");
        }

        teamRepository.delete(team);
    }

    public List<TeamLeadDto> getTeamLeads() {
        User user = getCurrentUser();
        Organization org = user.getOrganization();

        List<User> allOrgUsers = userRepository.findByOrganization(org);
        List<User> admins = allOrgUsers.stream()
                .filter(u -> u.getRole() == Role.ADMIN ||
                        (u.getCreatedBy() != null && u.getCreatedBy().getId().equals(user.getId()) && u.getRole() != Role.OWNER))
                .distinct()
                .toList();

        return admins.stream().map(admin -> {
            List<User> teamMates = allOrgUsers.stream()
                    .filter(m -> m.getCreatedBy() != null && m.getCreatedBy().getId().equals(admin.getId()))
                    .toList();

            String dept = admin.getTeam() != null ? admin.getTeam().getName() :
                    (admin.getDesignation() != null ? admin.getDesignation() : "Team Lead");

            return TeamLeadDto.builder()
                    .id(admin.getId())
                    .name(admin.getName())
                    .email(admin.getEmail())
                    .phone(admin.getPhone())
                    .designation(dept)
                    .username(admin.getUsername())
                    .memberCount(teamMates.size())
                    .teamMembers(teamMates)
                    .build();
        }).toList();
    }

    public User createTeamLead(User teamLead, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow();
        if (owner.getRole() != Role.OWNER) {
            throw new RuntimeException("Only Organization Owner can create Team Leads / Managers.");
        }
        String companyDomain = owner.getOrganization().getCompanyDomain().toLowerCase().trim();

        if (teamLead.getEmail() == null || !teamLead.getEmail().toLowerCase().endsWith("@" + companyDomain)) {
            throw new RuntimeException("Team Lead email must belong to @" + companyDomain);
        }

        String cleanEmail = teamLead.getEmail().toLowerCase().trim();
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new RuntimeException("An account with this email address already exists.");
        }

        String deptForCode = teamLead.getDesignation() != null ? teamLead.getDesignation() : "Development";
        String companyName = owner.getOrganization().getCompanyName().replaceAll("[^a-zA-Z]", "").toUpperCase();
        String compPrefix = companyName.length() >= 3 ? companyName.substring(0, 3) : (companyName + "TL").substring(0, 3);

        String deptPrefix = "DT";
        if (deptForCode != null && !deptForCode.trim().isEmpty()) {
            String clean = deptForCode.replaceAll("[^a-zA-Z ]", "").trim();
            String[] parts = clean.split("\\s+");
            if (parts.length >= 2) {
                deptPrefix = (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
            } else if (clean.length() >= 2) {
                deptPrefix = (clean.substring(0, 1) + clean.substring(clean.length() - 1)).toUpperCase();
            } else {
                deptPrefix = clean.toUpperCase() + "T";
            }
        }

        long codeSeq = userRepository.countByOrganization(owner.getOrganization()) + 1;
        String empCode = String.format("%s-%s%03d", deptPrefix, compPrefix, codeSeq);

        String rawPassword = (teamLead.getPassword() != null && !teamLead.getPassword().trim().isEmpty())
                ? teamLead.getPassword().trim()
                : "Welcome@123";

        teamLead.setPassword(passwordEncoder.encode(rawPassword));
        teamLead.setEmail(cleanEmail);
        teamLead.setEmployeeCode(empCode);
        teamLead.setCreatedBy(owner);
        teamLead.setRole(Role.ADMIN);
        teamLead.setOrganization(owner.getOrganization());
        teamLead.setCreatedAt(LocalDateTime.now());

        User savedLead = userRepository.save(teamLead);

        // Auto-create or link Organization Team for this department!
        String deptName = savedLead.getDesignation();
        if (deptName != null && !deptName.trim().isEmpty()) {
            String cleanDept = deptName.trim();
            Organization org = owner.getOrganization();

            Team team = teamRepository.findByOrganization(org).stream()
                    .filter(t -> t.getName().equalsIgnoreCase(cleanDept))
                    .findFirst()
                    .orElse(null);

            if (team == null) {
                team = Team.builder()
                        .name(cleanDept)
                        .description(cleanDept + " Department managed by " + savedLead.getName())
                        .lead(savedLead)
                        .organization(org)
                        .build();
            } else {
                team.setLead(savedLead);
            }
            Team savedTeam = teamRepository.save(team);
            savedLead.setTeam(savedTeam);
            savedLead = userRepository.save(savedLead);
        }

        return savedLead;
    }

    private OrganizationDto mapToDto(Organization org) {
        return OrganizationDto.builder()
                .id(org.getId())
                .companyName(org.getCompanyName())
                .companyCode(org.getCompanyCode())
                .companyDomain(org.getCompanyDomain())
                .logoUrl(org.getLogoUrl())
                .description(org.getDescription())
                .industry(org.getIndustry())
                .contactEmail(org.getContactEmail())
                .contactPhone(org.getContactPhone())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .build();
    }
}
