package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.DiscussionMessage;

import com.ganesh.taskmanager.entity.Organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscussionMessageRepository

        extends JpaRepository
        <DiscussionMessage, Long> {

    List<DiscussionMessage>

    findByOrganizationOrderByCreatedAtAsc(

            Organization organization
    );
}