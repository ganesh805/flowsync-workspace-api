package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.dto.OrganizationDto;
import com.ganesh.taskmanager.dto.OrganizationMetricsDto;
import com.ganesh.taskmanager.dto.TeamDto;
import com.ganesh.taskmanager.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    public ResponseEntity<OrganizationDto> getOrganization() {
        return ResponseEntity.ok(organizationService.getOrganization());
    }

    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrganizationDto> updateOrganization(@RequestBody OrganizationDto dto) {
        return ResponseEntity.ok(organizationService.updateOrganization(dto));
    }

    @PostMapping("/logo")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrganizationDto> uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(organizationService.uploadLogo(file));
    }

    @DeleteMapping("/logo")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OrganizationDto> removeLogo() {
        return ResponseEntity.ok(organizationService.removeLogo());
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<OrganizationMetricsDto> getMetrics() {
        return ResponseEntity.ok(organizationService.getMetrics());
    }

    @GetMapping("/teams")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<List<TeamDto>> getTeams() {
        return ResponseEntity.ok(organizationService.getTeams());
    }

    @PostMapping("/teams")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<TeamDto> createTeam(@RequestBody TeamDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.createTeam(dto));
    }

    @DeleteMapping("/teams/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        organizationService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/team-leads")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<com.ganesh.taskmanager.dto.TeamLeadDto>> getTeamLeads() {
        return ResponseEntity.ok(organizationService.getTeamLeads());
    }

    @PostMapping("/team-leads")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<com.ganesh.taskmanager.entity.User> createTeamLead(
            @RequestBody com.ganesh.taskmanager.entity.User teamLead,
            org.springframework.security.core.Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createTeamLead(teamLead, authentication.getName()));
    }
}
