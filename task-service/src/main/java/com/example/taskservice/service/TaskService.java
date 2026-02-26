package com.example.taskservice.service;

import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.entity.Task;
import com.example.taskservice.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Add a new task
     */
    public TaskResponse addTask(TaskRequest taskRequest) {
        Task task = new Task();
        task.setName(taskRequest.getName());
        task.setDescription(taskRequest.getDescription());

        Task savedTask = taskRepository.save(task);
        return convertToResponse(savedTask);
    }

    /**
     * Remove a task by name
     */
    public void removeTask(String taskName) {
        Task task = taskRepository.findByName(taskName)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskName));
        taskRepository.delete(task);
    }

    /**
     * Find a task by name
     */
    @Transactional(readOnly = true)
    public TaskResponse findTaskByName(String taskName) {
        Task task = taskRepository.findByName(taskName)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskName));
        return convertToResponse(task);
    }

    /**
     * Search for tasks by name (free text search)
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(String searchTerm) {
        List<Task> tasks = taskRepository.searchByName(searchTerm);
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update a task description
     */
    public TaskResponse updateTaskDescription(String taskName, String newDescription) {
        Task task = taskRepository.findByName(taskName)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskName));
        task.setDescription(newDescription);

        Task updatedTask = taskRepository.save(task);
        return convertToResponse(updatedTask);
    }

    /**
     * Get all tasks
     */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Task entity to TaskResponse DTO
     */
    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

}
