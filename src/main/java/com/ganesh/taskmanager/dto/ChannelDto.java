package com.ganesh.taskmanager.dto;

import com.ganesh.taskmanager.enums.ChannelType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelDto {
    private Long id;
    private String name;
    private String description;
    private ChannelType channelType;
    private Long organizationId;
    private Long teamId;
    private String teamName;
    private String createdByName;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean archived;
    private Integer memberCount;
    private Boolean isMember;
}
