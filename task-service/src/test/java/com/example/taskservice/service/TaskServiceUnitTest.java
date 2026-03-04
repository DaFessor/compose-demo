package com.example.taskservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.entity.Task;
import com.example.taskservice.repository.TaskRepository;

/**
 * Unit tests for TaskService that test business logic
 * without requiring a running database.
 *
 * Uses Mockito to mock the repository layer so all tests
 * can run with mocked database interactions.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Task Service Unit Tests")
class TaskServiceUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        // Create sample task entity
        sampleTask = new Task();
        sampleTask.setId(1L);
        sampleTask.setName("Complete Project");
        sampleTask.setDescription("Finish the project by Friday");
        sampleTask.setCreatedAt(LocalDateTime.now());
        sampleTask.setUpdatedAt(LocalDateTime.now());

        // Create sample task request
        taskRequest = new TaskRequest();
        taskRequest.setName("Complete Project");
        taskRequest.setDescription("Finish the project by Friday");
    }

    @Test
    @DisplayName("addTask - should successfully add a new task")
    void testAddTask_Success() {
        // Arrange
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // Act
        TaskResponse response = taskService.addTask(taskRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Complete Project", response.getName());
        assertEquals("Finish the project by Friday", response.getDescription());
        assertEquals(1L, response.getId());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("findTaskByName - should find a task by name")
    void testFindTaskByName_Success() {
        // Arrange
        when(taskRepository.findByName("Complete Project")).thenReturn(Optional.of(sampleTask));

        // Act
        TaskResponse response = taskService.findTaskByName("Complete Project");

        // Assert
        assertNotNull(response);
        assertEquals("Complete Project", response.getName());
        assertEquals("Finish the project by Friday", response.getDescription());
        verify(taskRepository, times(1)).findByName("Complete Project");
    }

    @Test
    @DisplayName("findTaskByName - should throw exception when task not found")
    void testFindTaskByName_NotFound() {
        // Arrange
        when(taskRepository.findByName("Unknown Task")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.findTaskByName("Unknown Task");
        });
        assertEquals("Task not found: Unknown Task", exception.getMessage());
        verify(taskRepository, times(1)).findByName("Unknown Task");
    }

    @Test
    @DisplayName("getAllTasks - should return all tasks")
    void testGetAllTasks_Success() {
        // Arrange
        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Review Code");
        task2.setDescription("Review pull requests");
        task2.setCreatedAt(LocalDateTime.now());
        task2.setUpdatedAt(LocalDateTime.now());

        when(taskRepository.findAll()).thenReturn(List.of(sampleTask, task2));

        // Act
        List<TaskResponse> responses = taskService.getAllTasks();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Complete Project", responses.get(0).getName());
        assertEquals("Review Code", responses.get(1).getName());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllTasks - should return empty list when no tasks exist")
    void testGetAllTasks_Empty() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(List.of());

        // Act
        List<TaskResponse> responses = taskService.getAllTasks();

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("searchTasks - should search for tasks by name")
    void testSearchTasks_Success() {
        // Arrange
        when(taskRepository.searchByName("Project")).thenReturn(List.of(sampleTask));

        // Act
        List<TaskResponse> responses = taskService.searchTasks("Project");

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Complete Project", responses.get(0).getName());
        verify(taskRepository, times(1)).searchByName("Project");
    }

    @Test
    @DisplayName("searchTasks - should return empty list when no matches found")
    void testSearchTasks_NoMatches() {
        // Arrange
        when(taskRepository.searchByName("NonExistent")).thenReturn(List.of());

        // Act
        List<TaskResponse> responses = taskService.searchTasks("NonExistent");

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(taskRepository, times(1)).searchByName("NonExistent");
    }

    @Test
    @DisplayName("removeTask - should delete a task successfully")
    void testRemoveTask_Success() {
        // Arrange
        when(taskRepository.findByName("Complete Project")).thenReturn(Optional.of(sampleTask));

        // Act
        taskService.removeTask("Complete Project");

        // Assert
        verify(taskRepository, times(1)).findByName("Complete Project");
        verify(taskRepository, times(1)).delete(sampleTask);
    }

    @Test
    @DisplayName("removeTask - should throw exception when task not found")
    void testRemoveTask_NotFound() {
        // Arrange
        when(taskRepository.findByName("Unknown Task")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.removeTask("Unknown Task");
        });
        assertEquals("Task not found: Unknown Task", exception.getMessage());
        verify(taskRepository, times(1)).findByName("Unknown Task");
        verify(taskRepository, never()).delete(any());
    }

    @Test
    @DisplayName("updateTaskDescription - should update task description successfully")
    void testUpdateTaskDescription_Success() {
        // Arrange
        when(taskRepository.findByName("Complete Project")).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        // Act
        TaskResponse response = taskService.updateTaskDescription("Complete Project", "Updated description");

        // Assert
        assertNotNull(response);
        assertEquals("Complete Project", response.getName());
        verify(taskRepository, times(1)).findByName("Complete Project");
        verify(taskRepository, times(1)).save(sampleTask);
    }

    @Test
    @DisplayName("updateTaskDescription - should throw exception when task not found")
    void testUpdateTaskDescription_NotFound() {
        // Arrange
        when(taskRepository.findByName("Unknown Task")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            taskService.updateTaskDescription("Unknown Task", "New description");
        });
        assertEquals("Task not found: Unknown Task", exception.getMessage());
        verify(taskRepository, times(1)).findByName("Unknown Task");
        verify(taskRepository, never()).save(any());
    }
}
