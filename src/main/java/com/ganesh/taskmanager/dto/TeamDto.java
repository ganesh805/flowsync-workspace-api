package com.ganesh.taskmanager.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDto {
    private Long id;
    private String name;
    private String description;
    private Long leadId;
    private String leadName;
    private long memberCount;
    private long activeTasks;
    private long completedTasks;
    private long pendingTasks;
    private double completionRate;
}
