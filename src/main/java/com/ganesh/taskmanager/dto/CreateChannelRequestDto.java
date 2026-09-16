package com.ganesh.taskmanager.dto;

import com.ganesh.taskmanager.enums.ChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChannelRequestDto {

    @NotBlank(message = "Channel name is required")
    @Size(min = 2, max = 50, message = "Channel name must be between 2 and 50 characters")
    private String name;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String description;

    @NotNull(message = "Channel type is required")
    private ChannelType channelType;

    private Long teamId;
}
