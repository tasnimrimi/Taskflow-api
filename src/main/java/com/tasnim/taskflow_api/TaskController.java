package com.tasnim.taskflow_api;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    @PostMapping
    public Task createTask(@RequestBody Map<String, String> request) {
        Task newTask = new Task();
        newTask.setTitle(request.get("title"));
        newTask.setCompleted(false);

        return taskRepository.save(newTask);
    }

    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Task task = taskRepository.findById(id).orElse(null);

        if (task == null) {
            return null;
        }

        if (request.containsKey("title")) {
            task.setTitle((String) request.get("title"));
        }

        if (request.containsKey("completed")) {
            task.setCompleted((Boolean) request.get("completed"));
        }

        return taskRepository.save(task);
    }

    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            return "Task not found";
        }

        taskRepository.deleteById(id);
        return "Task deleted successfully";
    }
}