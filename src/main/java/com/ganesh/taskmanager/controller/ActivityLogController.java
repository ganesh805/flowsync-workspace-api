package com.ganesh.taskmanager.controller;

import com.ganesh.taskmanager.entity.ActivityLog;
import com.ganesh.taskmanager.service.ActivityLogService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@CrossOrigin("*")

public class ActivityLogController {

    private final ActivityLogService
            activityLogService;

    @GetMapping

    public List<ActivityLog> getLogs() {

        return activityLogService
                .getAllLogs();
    }
}