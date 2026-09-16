package com.ganesh.taskmanager.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscussionMessageDto {
    private String message;
    private String taggedUser;
    private String channel;
}