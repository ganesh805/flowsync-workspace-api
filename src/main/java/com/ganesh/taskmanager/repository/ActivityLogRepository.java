package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Long> {
}