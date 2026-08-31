package com.tasnim.taskflow_api;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final List<Task> tasks = new ArrayList<>(List.of(
            new Task(1L, "Learn Java", true),
            new Task(2L, "Learn Spring Boot", false),
            new Task(3L, "Build TaskFlow API", false)
    ));

    private long nextId = 4;

    @GetMapping
    public List<Task> getTasks() {
        return tasks;
    }

    @PostMapping
    public Task createTask(@RequestBody Map<String, String> request) {
        String title = request.get("title");

        Task newTask = new Task(nextId, title, false);
        nextId++;

        tasks.add(newTask);

        return newTask;
    }
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        boolean removed = tasks.removeIf(task -> task.getId().equals(id));

        if (removed) {
            return "Task deleted successfully";
        }

        return "Task not found";
    }
    @PutMapping("/{id}")
    public Task updateTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        for (Task task : tasks) {
            if (task.getId().equals(id)) {

                if (request.containsKey("title")) {
                    task.setTitle((String) request.get("title"));
                }

                if (request.containsKey("completed")) {
                    task.setCompleted((Boolean) request.get("completed"));
                }

                return task;
            }
        }

        return null;
    }

}