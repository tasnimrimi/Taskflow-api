package com.tasnim.taskflow_api;


import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.junit.jupiter.api.Test;
import java.util.List;
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
import static org.hamcrest.Matchers.containsInAnyOrder;

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
    @Test
    void shouldFilterTasksThroughCompleteApplication()
            throws Exception {

        // Arrange: save tasks with different statuses
        Task completedTask =
                new Task(null, "Completed task", true);

        Task unfinishedTaskOne =
                new Task(null, "Unfinished task one", false);

        Task unfinishedTaskTwo =
                new Task(null, "Unfinished task two", false);

        taskRepository.saveAllAndFlush(
                List.of(
                        completedTask,
                        unfinishedTaskOne,
                        unfinishedTaskTwo
                )
        );

        // Act: request only unfinished tasks
        mockMvc.perform(get("/api/tasks")
                        .param("completed", "false"))
                .andExpect(status().isOk())

                // Assert: only two tasks were returned
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].completed").value(false))
                .andExpect(jsonPath("$[1].completed").value(false));
    }
    @Test
    void shouldSearchTasksThroughCompleteApplication() throws Exception {
        Task springTask = new Task(null, "Learn Spring Boot", false);
        Task javaTask = new Task(null, "Practise Java", false);
        Task testingTask = new Task(null, "SPRING testing", true);

        taskRepository.saveAllAndFlush(
                List.of(springTask, javaTask, testingTask)
        );

        mockMvc.perform(get("/api/tasks/search")
                        .param("title", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title",
                        containsInAnyOrder(
                                "Learn Spring Boot",
                                "SPRING testing"
                        )));
    }
    @Test
    void shouldPaginateTasksThroughCompleteApplication() throws Exception {
        Task firstTask = new Task(null, "First task", false);
        Task secondTask = new Task(null, "Second task", false);
        Task thirdTask = new Task(null, "Third task", true);
        Task fourthTask = new Task(null, "Fourth task", true);

        taskRepository.saveAllAndFlush(
                List.of(firstTask, secondTask, thirdTask, fourthTask)
        );

        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }
    @Test
    void shouldSortTasksThroughCompleteApplication() throws Exception {
        Task thirdAlphabetically =
                new Task(null, "Write documentation", false);

        Task firstAlphabetically =
                new Task(null, "Build API", false);

        Task secondAlphabetically =
                new Task(null, "Learn Spring", true);

        taskRepository.saveAllAndFlush(
                List.of(
                        thirdAlphabetically,
                        firstAlphabetically,
                        secondAlphabetically
                )
        );

        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sortBy", "title")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Build API"))
                .andExpect(jsonPath("$.content[1].title")
                        .value("Learn Spring"))
                .andExpect(jsonPath("$.content[2].title")
                        .value("Write documentation"));
    }
    @Test
    void shouldExposeOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title")
                        .value("TaskFlow API"))
                .andExpect(jsonPath("$.info.version")
                        .value("1.0.0"))
                .andExpect(jsonPath("$['paths']['/api/tasks']")
                        .exists())
                .andExpect(jsonPath("$['paths']['/api/tasks/page']")
                        .exists());
    }
}