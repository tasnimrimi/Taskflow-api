package com.tasnim.taskflow_api;

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
}