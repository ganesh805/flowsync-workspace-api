package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.entity.ActivityLog;

import com.ganesh.taskmanager.repository.ActivityLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class ActivityLogService {

    private final ActivityLogRepository
            activityLogRepository;

    // SAVE LOG

    public void save(

            String action,

            String details

    ) {

        ActivityLog log =

                ActivityLog.builder()

                        .action(action)

                        .details(details)

                        .performedBy("SYSTEM")

                        .build();

        activityLogRepository
                .save(log);
    }

    // GET ALL LOGS

    public List<ActivityLog>
    getAllLogs() {

        return activityLogRepository
                .findAll();
    }
}