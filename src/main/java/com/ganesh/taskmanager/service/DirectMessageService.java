package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.ChannelMessageDto;
import com.ganesh.taskmanager.entity.DiscussionMessage;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.enums.MessageType;
import com.ganesh.taskmanager.repository.DiscussionMessageRepository;
import com.ganesh.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectMessageService {

    private final DiscussionMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private User getUserByAuth() {
        String input = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(input)
                .or(() -> userRepository.findByUsernameIgnoreCase(input))
                .or(() -> userRepository.findByEmployeeCodeIgnoreCase(input))
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    @Transactional(readOnly = true)
    public List<User> getDirectContacts() {
        User user = getUserByAuth();
        return userRepository.findByOrganization(user.getOrganization()).stream()
                .filter(u -> u.getId() != null && !u.getId().equals(user.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ChannelMessageDto> getDirectMessages(Long recipientId, Pageable pageable) {
        User sender = getUserByAuth();
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));

        if (!sender.getOrganization().getId().equals(recipient.getOrganization().getId())) {
            throw new RuntimeException("Access Denied: Recipient belongs to another organization.");
        }

        Page<DiscussionMessage> msgs = messageRepository.findDirectMessagesPaginated(sender.getOrganization(), sender, recipient, pageable);
        return msgs.map(this::toMessageDto);
    }

    @Transactional
    public ChannelMessageDto sendDirectMessage(Long recipientId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty.");
        }

        User sender = getUserByAuth();
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));

        if (!sender.getOrganization().getId().equals(recipient.getOrganization().getId())) {
            throw new RuntimeException("Access Denied: Recipient belongs to another organization.");
        }

        DiscussionMessage msg = DiscussionMessage.builder()
                .message(content.trim())
                .channel("DIRECT")
                .sender(sender)
                .recipient(recipient)
                .organization(sender.getOrganization())
                .messageType(MessageType.TEXT)
                .createdAt(LocalDateTime.now())
                .build();

        DiscussionMessage saved = messageRepository.save(msg);

        notificationService.sendNotification(
                recipient,
                "New 1-on-1 private message from " + sender.getName() + ": \"" + content.trim() + "\""
        );

        return toMessageDto(saved);
    }

    private ChannelMessageDto toMessageDto(DiscussionMessage msg) {
        return ChannelMessageDto.builder()
                .id(msg.getId())
                .channelId(null)
                .channelName("DIRECT")
                .senderId(msg.getSender() != null ? msg.getSender().getId() : null)
                .senderName(msg.getSender() != null ? msg.getSender().getName() : "User")
                .senderProfileImage(msg.getSender() != null ? msg.getSender().getProfileImage() : null)
                .senderDesignation(msg.getSender() != null ? msg.getSender().getDesignation() : null)
                .content(msg.getMessage())
                .messageType(msg.getMessageType())
                .createdAt(msg.getCreatedAt())
                .editedAt(msg.getEditedAt())
                .deletedAt(msg.getDeletedAt())
                .isEdited(msg.getEditedAt() != null)
                .isDeleted(msg.getDeletedAt() != null)
                .build();
    }
}
