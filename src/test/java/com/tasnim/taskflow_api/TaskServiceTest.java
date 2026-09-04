package com.tasnim.taskflow_api;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.never;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
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

    @Test
    void shouldDeleteTaskWhenTaskExists() {
        // Arrange: pretend task 1 exists
        TaskRepository repository = mock(TaskRepository.class);
        TaskService service = new TaskService(repository);

        when(repository.existsById(1L))
                .thenReturn(true);

        // Act: ask the real service to delete it
        boolean result = service.deleteTask(1L);

        // Assert: check the result and the action
        assertTrue(result);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldNotDeleteTaskWhenTaskDoesNotExist() {
        // Arrange: pretend task 999 does not exist
        TaskRepository repository = mock(TaskRepository.class);
        TaskService service = new TaskService(repository);

        when(repository.existsById(999L))
                .thenReturn(false);

        // Act: try to delete it
        boolean result = service.deleteTask(999L);

        // Assert: report failure without requesting deletion
        assertFalse(result);
        verify(repository, never()).deleteById(999L);
    }
}