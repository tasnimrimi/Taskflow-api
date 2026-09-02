package com.tasnim.taskflow_api;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TaskServiceTest {

    @Test
    void shouldReturnNullWhenTaskDoesNotExist() {
        // Arrange: prepare the situation
        TaskRepository repository = mock(TaskRepository.class);
        TaskService service = new TaskService(repository);

        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        // Act: run the real service method
        Task result = service.getTaskById(999L);

        // Assert: check the result
        assertNull(result);
    }

    @Test
    void shouldReturnTaskWhenTaskExists() {
        // Arrange: prepare a fake repository and an example task
        TaskRepository repository = mock(TaskRepository.class);
        TaskService service = new TaskService(repository);

        Task existingTask = new Task(1L, "Learn backend testing", false);

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingTask));

        // Act: ask the real service for task 1
        Task result = service.getTaskById(1L);

        // Assert: it should return that same task
        assertSame(existingTask, result);
    }

    @Test
    void shouldUpdateCompletedWithoutChangingTitle() {
        // Arrange: start with a completed task
        TaskRepository repository = mock(TaskRepository.class);
        TaskService service = new TaskService(repository);

        Task existingTask = new Task(1L, "Learn testing", true);

        when(repository.findById(1L))
                .thenReturn(Optional.of(existingTask));

        when(repository.save(existingTask))
                .thenReturn(existingTask);

        // Act: leave the title unchanged, mark the task unfinished
        Task result = service.updateTask(1L, null, false);

        // Assert: check both requirements
        assertEquals("Learn testing", result.getTitle());
        assertFalse(result.isCompleted());
    }
}