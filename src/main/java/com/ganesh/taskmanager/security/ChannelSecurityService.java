package com.ganesh.taskmanager.security;

import com.ganesh.taskmanager.entity.Channel;
import com.ganesh.taskmanager.entity.Team;
import com.ganesh.taskmanager.entity.User;
import com.ganesh.taskmanager.enums.ChannelType;
import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.repository.ChannelMemberRepository;
import com.ganesh.taskmanager.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelSecurityService {

    private final ChannelMemberRepository channelMemberRepository;
    private final TeamRepository teamRepository;

    public void validateCanCreateChannel(User user, ChannelType channelType, Team targetTeam) {
        if (user.getRole() == Role.MEMBER) {
            throw new RuntimeException("Access Denied: Regular employees cannot create channels.");
        }

        if (user.getRole() == Role.ADMIN) {
            if (channelType == ChannelType.PUBLIC_ORG) {
                throw new RuntimeException("Access Denied: Team Leads cannot create organization-wide public channels.");
            }

            Team adminTeam = resolveAdminTeam(user);
            if (targetTeam != null && adminTeam != null && !targetTeam.getId().equals(adminTeam.getId())) {
                throw new RuntimeException("Access Denied: Team Leads can only create channels for their own team.");
            }
        }
    }

    public void validateCanAccessChannel(User user, Channel channel) {
        if (!channel.getOrganization().getId().equals(user.getOrganization().getId())) {
            throw new RuntimeException("Access Denied: Channel belongs to another organization.");
        }

        if (channel.getArchived() != null && channel.getArchived()) {
            throw new RuntimeException("Access Denied: Channel is archived.");
        }

        if (user.getRole() == Role.OWNER) {
            if (channel.getChannelType() == ChannelType.PRIVATE) {
                boolean isMember = channelMemberRepository.existsByChannelAndUser(channel, user);
                if (!isMember && (channel.getCreatedBy() == null || !channel.getCreatedBy().getId().equals(user.getId()))) {
                    throw new RuntimeException("Access Denied: Private channel access restricted to members.");
                }
            }
            return;
        }

        if (channel.getChannelType() == ChannelType.PUBLIC_ORG) {
            return; // All org members can access
        }

        if (channel.getChannelType() == ChannelType.PUBLIC_TEAM) {
            Team userTeam = user.getTeam() != null ? user.getTeam() : resolveAdminTeam(user);
            if (userTeam != null && channel.getTeam() != null && userTeam.getId().equals(channel.getTeam().getId())) {
                return;
            }
            throw new RuntimeException("Access Denied: You are not a member of this team.");
        }

        if (channel.getChannelType() == ChannelType.PRIVATE) {
            boolean isMember = channelMemberRepository.existsByChannelAndUser(channel, user);
            if (!isMember) {
                throw new RuntimeException("Access Denied: Private channel access restricted to members.");
            }
        }
    }

    public void validateCanManageChannel(User user, Channel channel) {
        if (user.getRole() == Role.MEMBER) {
            throw new RuntimeException("Access Denied: Regular employees cannot manage channels.");
        }

        if (user.getRole() == Role.OWNER) {
            return;
        }

        if (user.getRole() == Role.ADMIN) {
            Team adminTeam = resolveAdminTeam(user);
            if (channel.getTeam() != null && adminTeam != null && channel.getTeam().getId().equals(adminTeam.getId())) {
                return;
            }
            throw new RuntimeException("Access Denied: Team Leads can only manage their own team's channels.");
        }
    }

    private Team resolveAdminTeam(User user) {
        if (user.getTeam() != null) {
            return user.getTeam();
        }
        List<Team> teams = teamRepository.findByOrganization(user.getOrganization());
        return teams.stream()
                .filter(t -> t.getLead() != null && t.getLead().getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
    }
}
