package com.ganesh.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeePerformanceDto {

    private String employeeName;

    private String designation;

    private int assignedTasks;

    private int completedTasks;

    private int pendingTasks;

    private double completionRate;
}