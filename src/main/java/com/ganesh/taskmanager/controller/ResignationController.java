package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.entity.ResignationRequest;
import com.ganesh.taskmanager.service.ResignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resignation")
@RequiredArgsConstructor
public class ResignationController {

    private final ResignationService resignationService;

    @PostMapping
    public ResponseEntity<ResignationRequest> submitResignation(
            @RequestBody ResignationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resignationService.submitResignation(request, authentication.getName()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ResignationRequest>> getMyResignations(Authentication authentication) {
        return ResponseEntity.ok(resignationService.getMyResignations(authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public ResponseEntity<List<ResignationRequest>> getOrganizationResignations(Authentication authentication) {
        return ResponseEntity.ok(resignationService.getOrganizationResignations(authentication.getName()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OWNER')")
    public ResponseEntity<ResignationRequest> updateResignationStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String status = body.get("status");
        String feedback = body.get("feedback");
        String lastDayStr = body.get("approvedLastDay");
        if (lastDayStr == null || lastDayStr.trim().isEmpty()) {
            lastDayStr = body.get("lastWorkingDay");
        }

        LocalDate approvedLastDay = null;
        if (lastDayStr != null && !lastDayStr.trim().isEmpty()) {
            try {
                approvedLastDay = LocalDate.parse(lastDayStr.trim());
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(resignationService.updateResignationStatus(id, status, feedback, approvedLastDay, authentication.getName()));
    }
}
