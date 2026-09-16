package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Channel;
import com.ganesh.taskmanager.entity.ChannelMember;
import com.ganesh.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelMemberRepository extends JpaRepository<ChannelMember, Long> {

    List<ChannelMember> findByChannel(Channel channel);

    Optional<ChannelMember> findByChannelAndUser(Channel channel, User user);

    boolean existsByChannelAndUser(Channel channel, User user);

    void deleteByChannelAndUser(Channel channel, User user);
}
