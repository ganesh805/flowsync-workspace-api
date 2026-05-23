package com.ganesh.taskmanager.repository;

import com.ganesh.taskmanager.entity.Task;
import com.ganesh.taskmanager.entity.TaskComment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskCommentRepository
        extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTask(Task task);
}