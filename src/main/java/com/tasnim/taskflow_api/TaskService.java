package com.tasnim.taskflow_api;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public TaskService(
            TaskRepository taskRepository,
            AppUserRepository appUserRepository
    ) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
    }


    public List<Task> getAllTasks(String email) {
        AppUser owner = getUserByEmail(email);

        return taskRepository.findByOwner(owner);
    }

    public Page<Task> getAllTasks(
            String email,
            Pageable pageable
    ) {
        AppUser owner = getUserByEmail(email);

        return taskRepository.findByOwner(
                owner,
                pageable
        );
    }
    public List<Task> getTasksByCompleted(
            String email,
            boolean completed
    ) {
        AppUser owner = getUserByEmail(email);

        return taskRepository.findByOwnerAndCompleted(
                owner,
                completed
        );
    }
    public List<Task> searchTasksByTitle(
            String email,
            String title
    ) {
        AppUser owner = getUserByEmail(email);

        return taskRepository
                .findByOwnerAndTitleContainingIgnoreCase(
                        owner,
                        title
                );
    }

    public Task getTaskById(
            Long id,
            String email
    ) {
        AppUser owner = getUserByEmail(email);

        return taskRepository
                .findByIdAndOwner(id, owner)
                .orElse(null);
    }

    public Task createTask(
            String email,
            String title
    ) {
        AppUser owner = getUserByEmail(email);

        Task task = new Task();
        task.setTitle(title);
        task.setCompleted(false);
        task.setOwner(owner);

        return taskRepository.save(task);
    }

    public Task updateTask(
            Long id,
            String email,
            String title,
            Boolean completed
    ) {
        Task task = getTaskById(id, email);

        if (task == null) {
            return null;
        }

        if (title != null) {
            task.setTitle(title);
        }

        if (completed != null) {
            task.setCompleted(completed);
        }

        return taskRepository.save(task);
    }

    public boolean deleteTask(
            Long id,
            String email
    ) {
        Task task = getTaskById(id, email);

        if (task == null) {
            return false;
        }

        taskRepository.delete(task);
        return true;
    }

    private AppUser getUserByEmail(String email) {
        return appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user was not found"
                        )
                );
    }
}
