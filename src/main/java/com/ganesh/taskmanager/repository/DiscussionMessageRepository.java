package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Channel;
import com.ganesh.taskmanager.entity.DiscussionMessage;
import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, Long> {

    List<DiscussionMessage> findByOrganizationOrderByCreatedAtAsc(Organization organization);

    List<DiscussionMessage> findByOrganizationAndChannelOrderByCreatedAtAsc(Organization organization, String channel);

    @Query("SELECT m FROM DiscussionMessage m WHERE m.organization = :org AND m.channel = 'DIRECT' AND ((m.sender = :u1 AND m.recipient = :u2) OR (m.sender = :u2 AND m.recipient = :u1)) ORDER BY m.createdAt ASC")
    List<DiscussionMessage> findDirectMessages(@Param("org") Organization org, @Param("u1") User u1, @Param("u2") User u2);

    Page<DiscussionMessage> findByChannelEntityAndDeletedAtIsNullOrderByCreatedAtDesc(Channel channel, Pageable pageable);

    List<DiscussionMessage> findByChannelEntityAndDeletedAtIsNullOrderByCreatedAtAsc(Channel channel);

    @Query("SELECT m FROM DiscussionMessage m WHERE m.organization = :org AND m.channel = 'DIRECT' AND ((m.sender = :u1 AND m.recipient = :u2) OR (m.sender = :u2 AND m.recipient = :u1)) AND m.deletedAt IS NULL ORDER BY m.createdAt DESC")
    Page<DiscussionMessage> findDirectMessagesPaginated(@Param("org") Organization org, @Param("u1") User u1, @Param("u2") User u2, Pageable pageable);
}