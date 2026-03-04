package com.example.taskservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.exception.GlobalExceptionHandler;
import com.example.taskservice.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for TaskController that test REST endpoints
 * without requiring a running database.
 *
 * Uses standalone MockMvc setup with mocked service layer.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Task Controller Integration Tests")
class TaskControllerIntegrationTest {

    @Mock
    private TaskService taskService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TaskResponse sampleTask;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        // Setup MockMvc with the controller
        mockMvc = MockMvcBuilders.standaloneSetup(new TaskController(taskService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        // Create sample task response
        LocalDateTime now = LocalDateTime.now();
        sampleTask = new TaskResponse(
                1L,
                "Complete Project",
                "Finish the project by Friday",
                now,
                now);

        // Create sample task request
        taskRequest = new TaskRequest();
        taskRequest.setName("Complete Project");
        taskRequest.setDescription("Finish the project by Friday");
    }

    @Test
    @DisplayName("POST /api/tasks - should create a new task")
    void testAddTask_Success() throws Exception {
        // Arrange
        when(taskService.addTask(any(TaskRequest.class))).thenReturn(sampleTask);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Complete Project"))
                .andExpect(jsonPath("$.description").value("Finish the project by Friday"));
    }

    @Test
    @DisplayName("GET /api/tasks - should return all tasks")
    void testGetAllTasks_Success() throws Exception {
        // Arrange
        TaskResponse task2 = new TaskResponse(
                2L,
                "Review Code",
                "Review pull requests",
                LocalDateTime.now(),
                LocalDateTime.now());
        when(taskService.getAllTasks()).thenReturn(List.of(sampleTask, task2));

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Complete Project"))
                .andExpect(jsonPath("$[1].name").value("Review Code"));
    }

    @Test
    @DisplayName("GET /api/tasks - should return empty list when no tasks exist")
    void testGetAllTasks_Empty() throws Exception {
        // Arrange
        when(taskService.getAllTasks()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/tasks")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskName} - should return a specific task by name")
    void testGetTask_Success() throws Exception {
        // Arrange
        when(taskService.findTaskByName("Complete Project")).thenReturn(sampleTask);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/Complete Project")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Complete Project"));
    }

    @Test
    @DisplayName("GET /api/tasks/{taskName} - should return 404 when task not found")
    void testGetTask_NotFound() throws Exception {
        // Arrange
        when(taskService.findTaskByName("Unknown Task"))
                .thenThrow(new RuntimeException("Task not found: Unknown Task"));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/Unknown Task")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tasks/search - should search tasks by name")
    void testSearchTasks_Success() throws Exception {
        // Arrange
        when(taskService.searchTasks("Project")).thenReturn(List.of(sampleTask));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/search")
                .param("q", "Project")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Complete Project"));
    }

    @Test
    @DisplayName("GET /api/tasks/search - should return empty list when no matches found")
    void testSearchTasks_NoMatches() throws Exception {
        // Arrange
        when(taskService.searchTasks("NonExistent")).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/tasks/search")
                .param("q", "NonExistent")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PUT /api/tasks/{taskName} - should update task description")
    void testUpdateTask_Success() throws Exception {
        // Arrange
        TaskResponse updatedTask = new TaskResponse(
                1L,
                "Complete Project",
                "Updated description",
                LocalDateTime.now(),
                LocalDateTime.now());
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setDescription("Updated description");

        when(taskService.updateTaskDescription(eq("Complete Project"), any(String.class)))
                .thenReturn(updatedTask);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/Complete Project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @DisplayName("PUT /api/tasks/{taskName} - should return 404 when task not found")
    void testUpdateTask_NotFound() throws Exception {
        // Arrange
        TaskRequest updateRequest = new TaskRequest();
        updateRequest.setDescription("New description");

        when(taskService.updateTaskDescription(eq("Unknown Task"), any(String.class)))
                .thenThrow(new RuntimeException("Task not found: Unknown Task"));

        // Act & Assert
        mockMvc.perform(put("/api/tasks/Unknown Task")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{taskName} - should delete a task")
    void testDeleteTask_Success() throws Exception {
        // Arrange
        doNothing().when(taskService).removeTask("Complete Project");

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/Complete Project"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tasks/{taskName} - should return 404 when task not found")
    void testDeleteTask_NotFound() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Task not found: Unknown Task"))
                .when(taskService).removeTask("Unknown Task");

        // Act & Assert
        mockMvc.perform(delete("/api/tasks/Unknown Task"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tasks/health - should return health status")
    void testHealthCheck_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tasks/health")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Task Service is healthy"));
    }
}
