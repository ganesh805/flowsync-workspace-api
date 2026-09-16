package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.entity.*;
import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final UserRepository userRepository;
    private final DiscussionMessageRepository discussionRepository;
    private final TeamRepository teamRepository;
    private final NotificationService notificationService;

    private Team resolveUserTeam(User user) {
        if (user.getTeam() != null) {
            return user.getTeam();
        }
        List<Team> orgTeams = teamRepository.findByOrganization(user.getOrganization());
        if (user.getRole() == Role.ADMIN) {
            return orgTeams.stream()
                    .filter(t -> t.getLead() != null && t.getLead().getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
        } else if (user.getCreatedBy() != null) {
            Long creatorId = user.getCreatedBy().getId();
            return orgTeams.stream()
                    .filter(t -> t.getLead() != null && t.getLead().getId().equals(creatorId))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private User getUserByAuth() {
        String input = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(input)
                .or(() -> userRepository.findByUsernameIgnoreCase(input))
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    public List<User> getCompanyColleagues() {
        User user = getUserByAuth();
        return userRepository.findByOrganization(user.getOrganization()).stream()
                .filter(u -> u.getId() != null && !u.getId().equals(user.getId()))
                .toList();
    }

    private final ChannelRepository channelRepository;

    public DiscussionMessage sendMessage(String message, String taggedUser, String channel) {
        User sender = getUserByAuth();

        String targetChannel = (channel != null && !channel.trim().isEmpty()) ? channel.toUpperCase() : "ORGANIZATION";

        if ("ORGANIZATION".equalsIgnoreCase(targetChannel) && sender.getRole() == Role.MEMBER) {
            throw new RuntimeException("Access Denied: Employees cannot post in executive organization discussions.");
        }

        if (taggedUser == null || taggedUser.trim().isEmpty()) {
            Pattern pattern = Pattern.compile("@([A-Za-z0-9_]+)");
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                taggedUser = matcher.group(1);
            }
        }

        Team userTeam = "TEAM".equalsIgnoreCase(targetChannel) ? resolveUserTeam(sender) : null;

        Channel chEntity = null;
        if ("ORGANIZATION".equalsIgnoreCase(targetChannel)) {
            chEntity = channelRepository.findByOrganizationAndNameIgnoreCaseAndArchivedFalse(sender.getOrganization(), "general").orElse(null);
        } else if ("TEAM".equalsIgnoreCase(targetChannel) && userTeam != null) {
            chEntity = channelRepository.findByOrganizationAndTeamAndArchivedFalseOrderByNameAsc(sender.getOrganization(), userTeam).stream().findFirst().orElse(null);
        }

        DiscussionMessage msg = DiscussionMessage.builder()
                .message(message)
                .taggedUser(taggedUser)
                .channel(targetChannel)
                .channelEntity(chEntity)
                .createdAt(LocalDateTime.now())
                .sender(sender)
                .organization(sender.getOrganization())
                .team(userTeam)
                .build();

        DiscussionMessage saved = discussionRepository.save(msg);

        // Smart Mention Notification
        if (taggedUser != null && !taggedUser.trim().isEmpty()) {
            final String finalTag = taggedUser.trim();
            Optional<User> mentionedUser = userRepository.findByOrganization(sender.getOrganization()).stream()
                    .filter(u -> u.getName().equalsIgnoreCase(finalTag) || u.getUsername() != null && u.getUsername().equalsIgnoreCase(finalTag))
                    .findFirst();
            if (mentionedUser.isPresent() && !mentionedUser.get().getId().equals(sender.getId())) {
                notificationService.sendNotification(
                        mentionedUser.get(),
                        sender.getName() + " mentioned you in " + targetChannel + " discussion: \"" + message + "\""
                );
            }
        }

        return saved;
    }

    public DiscussionMessage sendDirectMessage(Long recipientId, String message) {
        User sender = getUserByAuth();
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));

        if (!sender.getOrganization().getId().equals(recipient.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized direct message outside organization.");
        }

        DiscussionMessage msg = DiscussionMessage.builder()
                .message(message)
                .channel("DIRECT")
                .createdAt(LocalDateTime.now())
                .sender(sender)
                .recipient(recipient)
                .organization(sender.getOrganization())
                .build();

        DiscussionMessage saved = discussionRepository.save(msg);

        notificationService.sendNotification(
                recipient,
                "New 1-on-1 private message from " + sender.getName() + ": \"" + message + "\""
        );

        return saved;
    }

    public List<DiscussionMessage> getDirectMessages(Long recipientId) {
        User sender = getUserByAuth();
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));

        return discussionRepository.findDirectMessages(sender.getOrganization(), sender, recipient);
    }

    public List<DiscussionMessage> getMessages(String channel) {
        User user = getUserByAuth();

        String targetChannel = (channel != null && !channel.trim().isEmpty()) ? channel.toUpperCase() : "ORGANIZATION";

        if ("ORGANIZATION".equalsIgnoreCase(targetChannel) && user.getRole() == Role.MEMBER) {
            throw new RuntimeException("Access Denied: Employees cannot view executive organization discussions.");
        }

        List<DiscussionMessage> allOrgChannelMsgs = discussionRepository.findByOrganizationAndChannelOrderByCreatedAtAsc(
                user.getOrganization(),
                targetChannel
        );

        if ("ORGANIZATION".equalsIgnoreCase(targetChannel) || user.getRole() == Role.OWNER) {
            return allOrgChannelMsgs;
        }

        // Isolate TEAM chat to the specific Team Lead / Department
        Team userTeam = resolveUserTeam(user);
        Long teamLeadId = (user.getRole() == Role.ADMIN) ? user.getId() : (user.getCreatedBy() != null ? user.getCreatedBy().getId() : null);

        return allOrgChannelMsgs.stream().filter(m -> {
            if (userTeam != null && m.getTeam() != null && m.getTeam().getId().equals(userTeam.getId())) {
                return true;
            }
            if (m.getSender() != null && m.getSender().getId().equals(user.getId())) {
                return true;
            }
            if (teamLeadId != null && m.getSender() != null) {
                if (m.getSender().getId().equals(teamLeadId)) return true;
                if (m.getSender().getCreatedBy() != null && m.getSender().getCreatedBy().getId().equals(teamLeadId)) return true;
            }
            return false;
        }).toList();
    }
}