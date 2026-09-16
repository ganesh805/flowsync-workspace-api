package com.ganesh.taskmanager.dto;

import com.ganesh.taskmanager.enums.MessageType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMessageDto {
    private Long id;
    private Long channelId;
    private String channelName;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private String senderDesignation;
    private String content;
    private MessageType messageType;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private LocalDateTime deletedAt;
    private Boolean isEdited;
    private Boolean isDeleted;
}
