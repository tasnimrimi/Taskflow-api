package com.tasnim.taskflow_api;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface TaskRepository
        extends JpaRepository<Task, Long> {

    Page<Task> findByOwner(
            AppUser owner,
            Pageable pageable
    );

    List<Task> findByOwner(AppUser owner);

    Optional<Task> findByIdAndOwner(
            Long id,
            AppUser owner
    );
    List<Task> findByOwnerAndCompleted(
            AppUser owner,
            boolean completed
    );
    List<Task> findByOwnerAndTitleContainingIgnoreCase(
            AppUser owner,
            String title
    );
}
