package com.tasnim.taskflow_api;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
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