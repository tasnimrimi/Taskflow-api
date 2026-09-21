package com.tasnim.taskflow_api;

import java.util.List;

import java.security.Principal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Locale;
import java.util.Set;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tasks")
@Tag(
        name = "Tasks",
        description = "Create, retrieve, update, delete, filter, search, and paginate tasks"
)
public class TaskController {
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("id", "title", "completed");

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "List or filter tasks")
    @GetMapping
    public List<Task> getTasks(
            @RequestParam(required = false) Boolean completed,
            Principal principal
    ) {
        String email = principal.getName();

        if (completed == null) {
            return taskService.getAllTasks(email);
        }

        return taskService.getTasksByCompleted(
                email,
                completed
        );
    }

    @Operation(summary = "Get a sorted page of tasks")
    @GetMapping("/page")
    public Page<Task> getTasksPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            Principal principal
    ) {

        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "page must be zero or greater"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "size must be between 1 and 100"
            );
        }

        String normalizedSortBy =
                sortBy.toLowerCase(Locale.ROOT);

        String normalizedDirection =
                direction.toLowerCase(Locale.ROOT);

        if (!ALLOWED_SORT_FIELDS.contains(normalizedSortBy)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "sortBy must be id, title, or completed"
            );
        }

        if (!normalizedDirection.equals("asc")
                && !normalizedDirection.equals("desc")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "direction must be asc or desc"
            );
        }

        Sort sort = normalizedDirection.equals("desc")
                ? Sort.by(normalizedSortBy).descending()
                : Sort.by(normalizedSortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return taskService.getAllTasks(
                principal.getName(),
                pageable
        );
    }

    @Operation(summary = "Search tasks by title")
    @GetMapping("/search")
    public List<Task> searchTasks(
            @RequestParam String title,
            Principal principal
    ) {
        return taskService.searchTasksByTitle(
                principal.getName(),
                title
        );
    }

    @Operation(summary = "Get one task by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(
            @PathVariable Long id,
            Principal principal
    ) {
        Task task = taskService.getTaskById(
                id,
                principal.getName()
        );

        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Create a task")
    @PostMapping
    public ResponseEntity<Task> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Principal principal
    ) {
        Task savedTask = taskService.createTask(
                principal.getName(),
                request.getTitle()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedTask);
    }
    @Operation(summary = "Partially update a task")
    @PatchMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            Principal principal
    ) {
        Task updatedTask = taskService.updateTask(
                id,
                principal.getName(),
                request.getTitle(),
                request.getCompleted()
        );

        if (updatedTask == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedTask);
    }

    @Operation(summary = "Delete a task")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            Principal principal
    ) {
        boolean deleted = taskService.deleteTask(
                id,
                principal.getName()
        );

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}