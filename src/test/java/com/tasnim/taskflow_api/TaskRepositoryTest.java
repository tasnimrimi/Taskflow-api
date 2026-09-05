package com.tasnim.taskflow_api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldSaveAndFindTask() {
        // Arrange: create a Java task
        Task task = new Task();
        task.setTitle("Learn database testing");
        task.setCompleted(false);

        // Act: save it into the test database
        Task savedTask = taskRepository.saveAndFlush(task);

        // Search for it using the generated ID
        Optional<Task> foundTask =
                taskRepository.findById(savedTask.getId());

        // Assert: confirm that it was saved and found
        assertTrue(foundTask.isPresent());
        assertEquals(
                "Learn database testing",
                foundTask.get().getTitle()
        );
    }

    @Test
    void shouldDeleteTask() {
        // Arrange: save a task in the temporary test database
        Task task = new Task();
        task.setTitle("Task to delete");
        task.setCompleted(false);

        Task savedTask = taskRepository.saveAndFlush(task);
        Long taskId = savedTask.getId();

        // Confirm that it currently exists
        assertTrue(taskRepository.existsById(taskId));

        // Act: delete it from the test database
        taskRepository.deleteById(taskId);
        taskRepository.flush();

        // Assert: confirm it no longer exists
        assertFalse(taskRepository.existsById(taskId));
    }
}