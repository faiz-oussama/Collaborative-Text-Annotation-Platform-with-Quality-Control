package com.annotations.demo.repository;

import com.annotations.demo.entity.Task;
import com.annotations.demo.entity.TaskProgress;
import com.annotations.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {
    Optional<TaskProgress> findByUserAndTask(User user, Task task);
}
