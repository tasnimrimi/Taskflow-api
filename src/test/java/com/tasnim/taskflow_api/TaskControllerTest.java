package com.tasnim.taskflow_api;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void shouldReturn404WhenTaskDoesNotExist() throws Exception {
        // Pretend task 999 does not exist
        when(taskService.getTaskById(999L))
                .thenReturn(null);

        // Request that task and check for HTTP 404
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnTaskWhenTaskExists() throws Exception {
        // Arrange: prepare a task and make the fake service return it
        Task task = new Task(1L, "Learn controller testing", false);

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        // Act and Assert: request task 1 and check the response
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Learn controller testing"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldReturn400WhenCreatingTaskWithBlankTitle() throws Exception {
        String requestBody = """
            {
              "title": "   "
            }
            """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.field").value("title"))
                .andExpect(jsonPath("$.message")
                        .value("Title is required"));
    }

    @Test
    void shouldCreateTaskAndReturn201() throws Exception {
        // Arrange: this is what the fake service will return
        Task savedTask = new Task(1L, "Learn API testing", false);

        when(taskService.createTask("Learn API testing"))
                .thenReturn(savedTask);

        String requestBody = """
            {
              "title": "Learn API testing"
            }
            """;

        // Act and Assert
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Learn API testing"))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void shouldUpdateTaskAndReturn200() throws Exception {
        // Arrange: title stays unchanged, completed becomes true
        Task updatedTask = new Task(1L, "Learn API testing", true);

        when(taskService.updateTask(1L, null, true))
                .thenReturn(updatedTask);

        String requestBody = """
            {
              "completed": true
            }
            """;

        // Act and Assert
        mockMvc.perform(patch("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("Learn API testing"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void shouldDeleteTaskAndReturn204() throws Exception {
        // Arrange: pretend the service successfully deleted task 1
        when(taskService.deleteTask(1L))
                .thenReturn(true);

        // Act and Assert
        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingMissingTask() throws Exception {
        // Arrange: pretend task 999 does not exist
        when(taskService.deleteTask(999L))
                .thenReturn(false);

        // Act and Assert
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }
}