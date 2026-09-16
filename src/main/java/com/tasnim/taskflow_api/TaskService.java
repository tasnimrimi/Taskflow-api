package com.tasnim.taskflow_api;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    public List<Task> getTasksByCompleted(boolean completed) {
        return taskRepository.findByCompleted(completed);
    }
    public List<Task> searchTasksByTitle(String title) {
        return taskRepository
                .findByTitleContainingIgnoreCase(title);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public Task createTask(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setCompleted(false);

        return taskRepository.save(task);
    }

    public Task updateTask(
            Long id,
            String title,
            Boolean completed
    ) {
        Task task = getTaskById(id);

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

    public boolean deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            return false;
        }

        taskRepository.deleteById(id);
        return true;
    }
}