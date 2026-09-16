package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Organization;
import com.ganesh.taskmanager.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByOrganization(Organization organization);
}
