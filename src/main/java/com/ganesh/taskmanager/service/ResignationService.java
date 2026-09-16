package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.entity.ResignationRequest;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.repository.ResignationRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResignationService {

    private final ResignationRepository resignationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private User getUserByAuth(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .or(() -> userRepository.findByUsernameIgnoreCase(email))
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    public ResignationRequest submitResignation(ResignationRequest request, String employeeEmail) {
        User employee = getUserByAuth(employeeEmail);
        if (employee.getRole() == Role.OWNER) {
            throw new RuntimeException("Organization Owner cannot submit a resignation request.");
        }

        // Policy check: default proposed last day to today + 90 days if null or earlier than 90 days
        LocalDate minimumNoticeDate = LocalDate.now().plusDays(90);
        if (request.getProposedLastDay() == null || request.getProposedLastDay().isBefore(minimumNoticeDate)) {
            request.setProposedLastDay(minimumNoticeDate);
        }

        request.setEmployee(employee);
        request.setOrganization(employee.getOrganization());
        request.setStatus(ResignationRequest.ResignationStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());

        ResignationRequest saved = resignationRepository.save(request);

        // Notify Team Lead or Owner
        if (employee.getCreatedBy() != null) {
            notificationService.sendNotification(
                    employee.getCreatedBy(),
                    "New resignation request submitted by " + employee.getName() + " (" + employee.getEmail() + ")."
            );
        }

        return saved;
    }

    public List<ResignationRequest> getMyResignations(String employeeEmail) {
        User employee = getUserByAuth(employeeEmail);
        return resignationRepository.findByEmployeeOrderByCreatedAtDesc(employee);
    }

    public List<ResignationRequest> getOrganizationResignations(String userEmail) {
        User user = getUserByAuth(userEmail);
        if (user.getRole() == Role.MEMBER) {
            throw new RuntimeException("Unauthorized: Employees cannot view team resignation requests.");
        }

        List<ResignationRequest> allOrgRequests = resignationRepository.findByOrganizationOrderByCreatedAtDesc(user.getOrganization());

        if (user.getRole() == Role.OWNER) {
            return allOrgRequests;
        }

        // For Team Leads (ADMIN), filter to only include members of their team/department
        final Long adminId = user.getId();
        final Long adminTeamId = user.getTeam() != null ? user.getTeam().getId() : null;

        return allOrgRequests.stream().filter(req -> {
            User emp = req.getEmployee();
            if (emp == null) return false;
            if (emp.getCreatedBy() != null && emp.getCreatedBy().getId().equals(adminId)) {
                return true;
            }
            if (adminTeamId != null && emp.getTeam() != null && emp.getTeam().getId().equals(adminTeamId)) {
                return true;
            }
            return false;
        }).toList();
    }

    public ResignationRequest updateResignationStatus(Long id, String statusStr, String feedback, LocalDate approvedLastDay, String userEmail) {
        User approver = getUserByAuth(userEmail);
        if (approver.getRole() == Role.MEMBER) {
            throw new RuntimeException("Unauthorized: Employees cannot approve/reject resignation requests.");
        }

        ResignationRequest req = resignationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resignation request not found"));

        if (!req.getOrganization().getId().equals(approver.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized organization request");
        }

        if (approver.getRole() == Role.ADMIN) {
            User emp = req.getEmployee();
            boolean isMyTeamMember = (emp != null && emp.getCreatedBy() != null && emp.getCreatedBy().getId().equals(approver.getId()))
                    || (emp != null && emp.getTeam() != null && approver.getTeam() != null && emp.getTeam().getId().equals(approver.getTeam().getId()));
            if (!isMyTeamMember) {
                throw new RuntimeException("Unauthorized: You can only process resignation requests for your own team employees.");
            }
        }

        ResignationRequest.ResignationStatus newStatus = ResignationRequest.ResignationStatus.valueOf(statusStr.toUpperCase());
        req.setStatus(newStatus);
        req.setAdminFeedback(feedback);
        if (approvedLastDay != null) {
            req.setApprovedLastDay(approvedLastDay);
        } else if (newStatus == ResignationRequest.ResignationStatus.APPROVED && req.getApprovedLastDay() == null) {
            req.setApprovedLastDay(req.getProposedLastDay());
        }
        req.setUpdatedAt(LocalDateTime.now());

        ResignationRequest updated = resignationRepository.save(req);

        // Notify Employee
        String lastDayNotice = (updated.getApprovedLastDay() != null) ? " Final Last Working Day set to: " + updated.getApprovedLastDay() + "." : "";
        notificationService.sendNotification(
                req.getEmployee(),
                "Your resignation request has been " + newStatus.name() + " by " + approver.getName() + "." + lastDayNotice
        );

        return updated;
    }
}
