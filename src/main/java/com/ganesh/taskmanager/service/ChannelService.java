package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.dto.ChannelDto;
import com.ganesh.taskmanager.dto.ChannelMessageDto;
import com.ganesh.taskmanager.dto.CreateChannelRequestDto;
import com.ganesh.taskmanager.entity.*;
import com.ganesh.taskmanager.enums.ChannelType;
import com.ganesh.taskmanager.enums.MessageType;
import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.repository.*;
import com.ganesh.taskmanager.security.ChannelSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final DiscussionMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ChannelSecurityService securityService;
    private final NotificationService notificationService;

    private User getUserByAuth() {
        String input = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(input)
                .or(() -> userRepository.findByUsernameIgnoreCase(input))
                .or(() -> userRepository.findByEmployeeCodeIgnoreCase(input))
                .orElseThrow(() -> new RuntimeException("User Not Found"));
    }

    private Team resolveUserTeam(User user) {
        if (user.getTeam() != null) {
            return user.getTeam();
        }
        List<Team> teams = teamRepository.findByOrganization(user.getOrganization());
        if (user.getRole() == Role.ADMIN) {
            return teams.stream()
                    .filter(t -> t.getLead() != null && t.getLead().getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Transactional
    public void ensureDefaultChannels(Organization org, User creator) {
        if (channelRepository.findByOrganizationAndNameIgnoreCaseAndArchivedFalse(org, "general").isEmpty()) {
            Channel general = Channel.builder()
                    .name("general")
                    .description("General organization-wide discussion and updates")
                    .channelType(ChannelType.PUBLIC_ORG)
                    .organization(org)
                    .createdBy(creator)
                    .build();
            channelRepository.save(general);
        }

        if (channelRepository.findByOrganizationAndNameIgnoreCaseAndArchivedFalse(org, "announcements").isEmpty()) {
            Channel announcements = Channel.builder()
                    .name("announcements")
                    .description("Important company announcements and news")
                    .channelType(ChannelType.PUBLIC_ORG)
                    .organization(org)
                    .createdBy(creator)
                    .build();
            channelRepository.save(announcements);
        }
    }

    @Transactional(readOnly = true)
    public List<ChannelDto> getChannelsForCurrentUser() {
        User user = getUserByAuth();
        ensureDefaultChannels(user.getOrganization(), user);

        Team userTeam = resolveUserTeam(user);
        List<Channel> allOrgChannels = channelRepository.findByOrganizationAndArchivedFalseOrderByNameAsc(user.getOrganization());

        return allOrgChannels.stream()
                .filter(c -> isChannelAccessible(c, user, userTeam))
                .map(c -> toChannelDto(c, user))
                .toList();
    }

    private boolean isChannelAccessible(Channel c, User user, Team userTeam) {
        if (c.getCreatedBy() != null && c.getCreatedBy().getId().equals(user.getId())) {
            return true;
        }
        if (channelMemberRepository.existsByChannelAndUser(c, user)) {
            return true;
        }

        if (user.getRole() == Role.OWNER || user.getRole() == Role.ADMIN) {
            return c.getChannelType() != ChannelType.PRIVATE;
        }

        if (c.getChannelType() == ChannelType.PUBLIC_ORG) {
            return true;
        }

        if (c.getChannelType() == ChannelType.PUBLIC_TEAM) {
            if (c.getTeam() == null) {
                return true;
            }
            return userTeam != null && c.getTeam().getId().equals(userTeam.getId());
        }

        return false;
    }

    @Transactional
    public ChannelDto createChannel(CreateChannelRequestDto dto) {
        User user = getUserByAuth();
        Team targetTeam = null;

        if (dto.getTeamId() != null) {
            targetTeam = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            if (!targetTeam.getOrganization().getId().equals(user.getOrganization().getId())) {
                throw new RuntimeException("Team belongs to another organization");
            }
        }

        securityService.validateCanCreateChannel(user, dto.getChannelType(), targetTeam);

        String cleanName = dto.getName().trim().toLowerCase().replaceAll("[^a-z0-9_-]", "-");

        Optional<Channel> existing = channelRepository.findByOrganizationAndNameIgnoreCaseAndArchivedFalse(user.getOrganization(), cleanName);
        if (existing.isPresent()) {
            throw new RuntimeException("A channel with name '#" + cleanName + "' already exists in your organization.");
        }

        Channel channel = Channel.builder()
                .name(cleanName)
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .channelType(dto.getChannelType())
                .organization(user.getOrganization())
                .team(targetTeam)
                .createdBy(user)
                .build();

        Channel saved = channelRepository.save(channel);

        // Add creator as member
        ChannelMember member = ChannelMember.builder()
                .channel(saved)
                .user(user)
                .build();
        channelMemberRepository.save(member);

        return toChannelDto(saved, user);
    }

    @Transactional(readOnly = true)
    public ChannelDto getChannelById(Long channelId) {
        User user = getUserByAuth();
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        securityService.validateCanAccessChannel(user, channel);
        return toChannelDto(channel, user);
    }

    @Transactional(readOnly = true)
    public Page<ChannelMessageDto> getChannelMessages(Long channelId, Pageable pageable) {
        User user = getUserByAuth();
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        securityService.validateCanAccessChannel(user, channel);

        Page<DiscussionMessage> msgs = messageRepository.findByChannelEntityAndDeletedAtIsNullOrderByCreatedAtDesc(channel, pageable);
        return msgs.map(this::toMessageDto);
    }

    @Transactional
    public ChannelMessageDto sendChannelMessage(Long channelId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty.");
        }

        User sender = getUserByAuth();
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        securityService.validateCanAccessChannel(sender, channel);

        DiscussionMessage msg = DiscussionMessage.builder()
                .message(content.trim())
                .channel(channel.getName())
                .channelEntity(channel)
                .sender(sender)
                .organization(sender.getOrganization())
                .team(channel.getTeam())
                .messageType(MessageType.TEXT)
                .createdAt(LocalDateTime.now())
                .build();

        DiscussionMessage saved = messageRepository.save(msg);
        return toMessageDto(saved);
    }

    @Transactional
    public ChannelMessageDto editMessage(Long messageId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty.");
        }

        User user = getUserByAuth();
        DiscussionMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!msg.getSender().getId().equals(user.getId())) {
            throw new RuntimeException("Access Denied: You can only edit your own messages.");
        }

        msg.setMessage(content.trim());
        msg.setEditedAt(LocalDateTime.now());
        DiscussionMessage saved = messageRepository.save(msg);
        return toMessageDto(saved);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        User user = getUserByAuth();
        DiscussionMessage msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!msg.getSender().getId().equals(user.getId()) && user.getRole() != Role.OWNER) {
            throw new RuntimeException("Access Denied: You can only delete your own messages.");
        }

        msg.setDeletedAt(LocalDateTime.now());
        messageRepository.save(msg);
    }

    @Transactional(readOnly = true)
    public List<User> getChannelMembers(Long channelId) {
        User user = getUserByAuth();
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        securityService.validateCanAccessChannel(user, channel);

        List<ChannelMember> members = channelMemberRepository.findByChannel(channel);
        return members.stream().map(ChannelMember::getUser).toList();
    }

    @Transactional
    public void addChannelMember(Long channelId, Long targetUserId) {
        User currentUser = getUserByAuth();
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        securityService.validateCanManageChannel(currentUser, channel);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (!targetUser.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("User belongs to another organization");
        }

        if (!channelMemberRepository.existsByChannelAndUser(channel, targetUser)) {
            ChannelMember member = ChannelMember.builder()
                    .channel(channel)
                    .user(targetUser)
                    .build();
            channelMemberRepository.save(member);

            notificationService.sendNotification(
                    targetUser,
                    "You were added to #" + channel.getName() + " by " + currentUser.getName()
            );
        }
    }

    @Transactional
    public void removeChannelMember(Long channelId, Long targetUserId) {
        User currentUser = getUserByAuth();
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        securityService.validateCanManageChannel(currentUser, channel);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        channelMemberRepository.deleteByChannelAndUser(channel, targetUser);
    }

    private ChannelDto toChannelDto(Channel channel, User user) {
        List<ChannelMember> members = channelMemberRepository.findByChannel(channel);
        boolean isMem = members.stream().anyMatch(m -> m.getUser().getId().equals(user.getId()));

        return ChannelDto.builder()
                .id(channel.getId())
                .name(channel.getName())
                .description(channel.getDescription())
                .channelType(channel.getChannelType())
                .organizationId(channel.getOrganization().getId())
                .teamId(channel.getTeam() != null ? channel.getTeam().getId() : null)
                .teamName(channel.getTeam() != null ? channel.getTeam().getName() : null)
                .createdByName(channel.getCreatedBy() != null ? channel.getCreatedBy().getName() : null)
                .createdById(channel.getCreatedBy() != null ? channel.getCreatedBy().getId() : null)
                .createdAt(channel.getCreatedAt())
                .updatedAt(channel.getUpdatedAt())
                .archived(channel.getArchived())
                .memberCount(members.size())
                .isMember(isMem)
                .build();
    }

    private ChannelMessageDto toMessageDto(DiscussionMessage msg) {
        return ChannelMessageDto.builder()
                .id(msg.getId())
                .channelId(msg.getChannelEntity() != null ? msg.getChannelEntity().getId() : null)
                .channelName(msg.getChannel())
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
