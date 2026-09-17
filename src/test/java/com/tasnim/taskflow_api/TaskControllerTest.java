package com.tasnim.taskflow_api;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
    @Test
    void shouldReturnFilteredTasksWhenCompletedIsProvided()
            throws Exception {

        // Arrange: fake service returns one completed task
        Task completedTask =
                new Task(1L, "Completed task", true);

        when(taskService.getTasksByCompleted(true))
                .thenReturn(List.of(completedTask));

        // Act and Assert
        mockMvc.perform(get("/api/tasks")
                        .param("completed", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title")
                        .value("Completed task"))
                .andExpect(jsonPath("$[0].completed").value(true));
    }
    @Test
    void shouldReturnTasksMatchingTitleSearch()
            throws Exception {

        // Arrange
        Task matchingTask =
                new Task(1L, "Learn Spring Boot", false);

        when(taskService.searchTasksByTitle("spring"))
                .thenReturn(List.of(matchingTask));

        // Act and Assert
        mockMvc.perform(get("/api/tasks/search")
                        .param("title", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title")
                        .value("Learn Spring Boot"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }
    @Test
    void shouldReturnRequestedPageOfTasks() throws Exception {
        PageRequest pageable = PageRequest.of(
                0,
                2,
                Sort.by("id").ascending()
        );

        Task firstTask = new Task(1L, "First task", false);
        Task secondTask = new Task(2L, "Second task", true);

        Page<Task> servicePage =
                new PageImpl<>(List.of(firstTask, secondTask), pageable, 4);

        when(taskService.getAllTasks(pageable))
                .thenReturn(servicePage);

        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("First task"))
                .andExpect(jsonPath("$.content[1].title").value("Second task"))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }
    @Test
    void shouldReturnTasksSortedByTitleDescending() throws Exception {
        PageRequest pageable = PageRequest.of(
                0,
                2,
                Sort.by("title").descending()
        );

        Task firstTask = new Task(2L, "Test pagination", false);
        Task secondTask = new Task(1L, "Learn Spring", false);

        Page<Task> servicePage =
                new PageImpl<>(List.of(firstTask, secondTask), pageable, 2);

        when(taskService.getAllTasks(pageable))
                .thenReturn(servicePage);

        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "title")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title")
                        .value("Test pagination"))
                .andExpect(jsonPath("$.content[1].title")
                        .value("Learn Spring"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }
    @Test
    void shouldReturn400ForInvalidSortField() throws Exception {
        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "password")
                        .param("direction", "asc"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturn400ForInvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "title")
                        .param("direction", "sideways"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturn400ForNegativePageNumber() throws Exception {
        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturn400WhenPageSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
    @Test
    void shouldReturn400WhenPageSizeExceedsMaximum() throws Exception {
        mockMvc.perform(get("/api/tasks/page")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }
}