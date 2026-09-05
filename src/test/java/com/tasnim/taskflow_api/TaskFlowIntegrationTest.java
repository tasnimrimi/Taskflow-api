package com.tasnim.taskflow_api;


import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:taskflow-integration"
})
@AutoConfigureMockMvc
@Transactional
class TaskFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldGetTaskThroughCompleteApplication() throws Exception {
        // Arrange: save a task in the temporary database
        Task task = new Task();
        task.setTitle("Test complete workflow");
        task.setCompleted(false);

        Task savedTask = taskRepository.saveAndFlush(task);

        // Act and Assert: request it through the real controller
        mockMvc.perform(get("/api/tasks/" + savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(savedTask.getId()))
                .andExpect(jsonPath("$.title")
                        .value("Test complete workflow"))
                .andExpect(jsonPath("$.completed").value(false));
    }
    @Test
    void shouldCreateTaskThroughCompleteApplication() throws Exception {
        String requestBody = """
            {
              "title": "Build complete integration test"
            }
            """;

        // Send JSON through the real controller and service
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title")
                        .value("Build complete integration test"))
                .andExpect(jsonPath("$.completed").value(false));

        // Confirm that the real repository can find the saved row
        assertEquals(1, taskRepository.count());

        Task savedTask = taskRepository.findAll().get(0);

        assertEquals(
                "Build complete integration test",
                savedTask.getTitle()
        );
        assertEquals(false, savedTask.isCompleted());
    }

    @Test
    void shouldUpdateTaskThroughCompleteApplication() throws Exception {
        // Arrange: save an unfinished task
        Task task = new Task();
        task.setTitle("Learn full integration testing");
        task.setCompleted(false);

        Task savedTask = taskRepository.saveAndFlush(task);

        String requestBody = """
            {
              "completed": true
            }
            """;

        // Act: update it through the complete API
        mockMvc.perform(patch("/api/tasks/" + savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title")
                        .value("Learn full integration testing"))
                .andExpect(jsonPath("$.completed").value(true));

        // Assert: read H2 and confirm the change was saved
        Task updatedTask = taskRepository
                .findById(savedTask.getId())
                .orElseThrow();

        assertEquals(
                "Learn full integration testing",
                updatedTask.getTitle()
        );
        assertTrue(updatedTask.isCompleted());
    }
    @Test
    void shouldDeleteTaskThroughCompleteApplication() throws Exception {
        // Arrange: save a task in the temporary database
        Task task = new Task();
        task.setTitle("Delete through integration test");
        task.setCompleted(false);

        Task savedTask = taskRepository.saveAndFlush(task);
        Long taskId = savedTask.getId();

        // Confirm it exists before deletion
        assertTrue(taskRepository.existsById(taskId));

        // Act: delete it through the complete API
        mockMvc.perform(delete("/api/tasks/" + taskId))
                .andExpect(status().isNoContent());

        // Assert: confirm H2 no longer contains it
        assertFalse(taskRepository.existsById(taskId));
    }
}