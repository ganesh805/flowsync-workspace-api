package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Channel;
import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.Team;
import com.ganesh.taskmanager.enums.ChannelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    List<Channel> findByOrganizationAndArchivedFalseOrderByNameAsc(Organization organization);

    List<Channel> findByOrganizationAndChannelTypeAndArchivedFalseOrderByNameAsc(Organization organization, ChannelType channelType);

    List<Channel> findByOrganizationAndTeamAndArchivedFalseOrderByNameAsc(Organization organization, Team team);

    Optional<Channel> findByOrganizationAndNameIgnoreCaseAndArchivedFalse(Organization organization, String name);

    @Query("SELECT DISTINCT c FROM Channel c LEFT JOIN ChannelMember cm ON cm.channel = c " +
           "WHERE c.organization = :org AND c.archived = false AND (" +
           "c.channelType = com.ganesh.taskmanager.enums.ChannelType.PUBLIC_ORG OR " +
           "(c.channelType = com.ganesh.taskmanager.enums.ChannelType.PUBLIC_TEAM AND c.team = :userTeam) OR " +
           "(c.channelType = com.ganesh.taskmanager.enums.ChannelType.PRIVATE AND cm.user.id = :userId)) " +
           "ORDER BY c.name ASC")
    List<Channel> findAccessibleChannelsForUser(@Param("org") Organization org, @Param("userTeam") Team userTeam, @Param("userId") Long userId);

    @Query("SELECT DISTINCT c FROM Channel c LEFT JOIN ChannelMember cm ON cm.channel = c " +
           "WHERE c.organization = :org AND c.archived = false AND (" +
           "c.channelType = com.ganesh.taskmanager.enums.ChannelType.PUBLIC_ORG OR " +
           "c.channelType = com.ganesh.taskmanager.enums.ChannelType.PUBLIC_TEAM OR " +
           "(c.channelType = com.ganesh.taskmanager.enums.ChannelType.PRIVATE AND cm.user.id = :userId)) " +
           "ORDER BY c.name ASC")
    List<Channel> findAccessibleChannelsForOwner(@Param("org") Organization org, @Param("userId") Long userId);
}
