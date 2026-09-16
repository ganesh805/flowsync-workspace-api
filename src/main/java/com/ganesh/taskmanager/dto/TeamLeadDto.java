package com.ganesh.taskmanager.dto;

import com.ganesh.taskmanager.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamLeadDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String designation;
    private String username;
    private long memberCount;
    private List<User> teamMembers;
}
