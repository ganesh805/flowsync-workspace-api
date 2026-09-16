package com.ganesh.taskmanager.config;

import com.ganesh.taskmanager.entity.*;
import com.ganesh.taskmanager.enums.ChannelType;
import com.ganesh.taskmanager.enums.Role;
import com.ganesh.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final TeamRepository teamRepository;
    private final ChannelRepository channelRepository;
    private final ChannelMemberRepository channelMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Create Default Organization
        Organization org = Organization.builder()
                .companyName("FlowSync Technologies")
                .companyCode("FLOW123")
                .companyDomain("flowsync.com")
                .createdAt(LocalDateTime.now())
                .build();
        org = organizationRepository.save(org);

        String defaultPass = passwordEncoder.encode("password123");

        // 2. Create Owner Account
        User owner = User.builder()
                .name("CEO Owner")
                .email("owner@flowsync.com")
                .username("ceo_owner")
                .password(defaultPass)
                .role(Role.OWNER)
                .designation("Chief Executive Officer")
                .organization(org)
                .createdAt(LocalDateTime.now())
                .build();
        owner = userRepository.save(owner);

        // 3. Create Admin Account
        User admin = User.builder()
                .name("Alex Admin")
                .email("admin@flowsync.com")
                .username("alex_admin")
                .password(defaultPass)
                .role(Role.ADMIN)
                .designation("Engineering Director")
                .organization(org)
                .createdBy(owner)
                .createdAt(LocalDateTime.now())
                .build();
        admin = userRepository.save(admin);

        // 4. Create Employee Accounts
        User emp1 = User.builder()
                .name("Saradha Tech")
                .email("saradha@flowsync.com")
                .username("saradha_dev")
                .password(defaultPass)
                .role(Role.MEMBER)
                .designation("Senior Software Engineer")
                .organization(org)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();
        emp1 = userRepository.save(emp1);

        User emp2 = User.builder()
                .name("Gnaneswar Product")
                .email("gnaneswar@flowsync.com")
                .username("gnaneswar_p")
                .password(defaultPass)
                .role(Role.MEMBER)
                .designation("Product Manager")
                .organization(org)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();
        emp2 = userRepository.save(emp2);

        // 5. Create Default Team
        Team engTeam = Team.builder()
                .name("Engineering Team")
                .description("Core Product & Frontend/Backend Development")
                .organization(org)
                .lead(admin)
                .createdAt(LocalDateTime.now())
                .build();
        engTeam = teamRepository.save(engTeam);

        admin.setTeam(engTeam);
        emp1.setTeam(engTeam);
        userRepository.save(admin);
        userRepository.save(emp1);

        // 6. Create Default Channels
        Channel general = Channel.builder()
                .name("general")
                .description("General organization-wide discussion and company news")
                .channelType(ChannelType.PUBLIC_ORG)
                .organization(org)
                .createdBy(owner)
                .createdAt(LocalDateTime.now())
                .build();
        general = channelRepository.save(general);

        Channel announcements = Channel.builder()
                .name("announcements")
                .description("Official company announcements")
                .channelType(ChannelType.PUBLIC_ORG)
                .organization(org)
                .createdBy(owner)
                .createdAt(LocalDateTime.now())
                .build();
        announcements = channelRepository.save(announcements);

        Channel engineering = Channel.builder()
                .name("engineering")
                .description("Engineering discussions & architecture planning")
                .channelType(ChannelType.PUBLIC_TEAM)
                .team(engTeam)
                .organization(org)
                .createdBy(admin)
                .createdAt(LocalDateTime.now())
                .build();
        engineering = channelRepository.save(engineering);

        // Add creator as member
        channelMemberRepository.save(ChannelMember.builder().channel(general).user(owner).build());
        channelMemberRepository.save(ChannelMember.builder().channel(general).user(admin).build());
        channelMemberRepository.save(ChannelMember.builder().channel(general).user(emp1).build());
        channelMemberRepository.save(ChannelMember.builder().channel(general).user(emp2).build());

        channelMemberRepository.save(ChannelMember.builder().channel(engineering).user(admin).build());
        channelMemberRepository.save(ChannelMember.builder().channel(engineering).user(emp1).build());
    }
}
