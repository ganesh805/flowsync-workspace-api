package com.ganesh.taskmanager.service;

import com.ganesh.taskmanager.entity.*;

import com.ganesh.taskmanager.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class DiscussionService {

    private final UserRepository
            userRepository;

    private final DiscussionMessageRepository
            discussionRepository;

    public DiscussionMessage sendMessage(

            String message

    ) {

        String email =

                SecurityContextHolder

                        .getContext()

                        .getAuthentication()

                        .getName();

        User user = userRepository

                .findByEmail(email)

                .orElseThrow();

        /* TAGGED USER */

        String taggedUser = null;

        Pattern pattern =

                Pattern.compile("@(\\w+)");

        Matcher matcher =

                pattern.matcher(message);

        if(matcher.find()) {

            taggedUser =

                    matcher.group(1);
        }

        DiscussionMessage msg =

                DiscussionMessage.builder()

                        .message(message)

                        .taggedUser(taggedUser)

                        .createdAt(
                                LocalDateTime.now()
                        )

                        .sender(user)

                        .organization(
                                user.getOrganization()
                        )

                        .build();

        return discussionRepository
                .save(msg);
    }

    public List<DiscussionMessage>
    getMessages() {

        String email =

                SecurityContextHolder

                        .getContext()

                        .getAuthentication()

                        .getName();

        User user = userRepository

                .findByEmail(email)

                .orElseThrow();

        return discussionRepository

                .findByOrganizationOrderByCreatedAtAsc(

                        user.getOrganization()
                );
    }
}