package com.example.taskservice.controller;

import com.example.taskservice.dto.TaskRequest;
import com.example.taskservice.dto.TaskResponse;
import com.example.taskservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Add a new task
     * POST /api/tasks
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> addTask(@RequestBody TaskRequest taskRequest) {
        TaskResponse response = taskService.addTask(taskRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all tasks
     * GET /api/tasks
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        List<TaskResponse> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Find or search for tasks
     * GET /api/tasks/search?q=searchTerm
     */
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponse>> searchTasks(@RequestParam("q") String searchTerm) {
        List<TaskResponse> tasks = taskService.searchTasks(searchTerm);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Get a specific task by name
     * GET /api/tasks/{taskName}
     */
    @GetMapping("/{taskName}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable String taskName) {
        TaskResponse task = taskService.findTaskByName(taskName);
        return ResponseEntity.ok(task);
    }

    /**
     * Update task description
     * PUT /api/tasks/{taskName}
     */
    @PutMapping(value = "/{taskName}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable String taskName,
            @RequestBody TaskRequest taskRequest) {
        TaskResponse response = taskService.updateTaskDescription(taskName, taskRequest.getDescription());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a task
     * DELETE /api/tasks/{taskName}
     */
    @DeleteMapping("/{taskName}")
    public ResponseEntity<Void> deleteTask(@PathVariable String taskName) {
        taskService.removeTask(taskName);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check endpoint
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Task Service is healthy");
    }

}
