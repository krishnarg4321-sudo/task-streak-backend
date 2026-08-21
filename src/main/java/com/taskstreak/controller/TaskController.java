package com.taskstreak.controller;

import com.taskstreak.dto.TaskDtos.CreateTaskRequest;
import com.taskstreak.dto.TaskDtos.HistoryResponse;
import com.taskstreak.dto.TaskDtos.UpdateTaskStatusRequest;
import com.taskstreak.dto.TaskDtos.UpdateTaskTimerRequest;
import com.taskstreak.model.ChecklistItem;
import com.taskstreak.model.Task;
import com.taskstreak.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@AuthenticationPrincipal String userId,
                                           @RequestBody CreateTaskRequest request) {
        Task created = taskService.createTask(userId, request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/today")
    public ResponseEntity<List<Task>> getTodayTasks(@AuthenticationPrincipal String userId) {
        List<Task> tasks = taskService.getTodayTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@AuthenticationPrincipal String userId,
                                           @PathVariable("id") String id) {
        Task task = taskService.getTaskById(id, userId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@AuthenticationPrincipal String userId,
                                                 @PathVariable("id") String id,
                                                 @RequestBody UpdateTaskStatusRequest request) {
        Task updated = taskService.updateTaskStatus(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/timer")
    public ResponseEntity<Task> updateTaskTimer(@AuthenticationPrincipal String userId,
                                                @PathVariable("id") String id,
                                                @RequestBody UpdateTaskTimerRequest request) {
        Task updated = taskService.updateTaskTimer(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/checklist")
    public ResponseEntity<Task> updateChecklist(@AuthenticationPrincipal String userId,
                                                @PathVariable("id") String id,
                                                @RequestBody List<ChecklistItem> checklist) {
        Task updated = taskService.updateChecklist(id, userId, checklist);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@AuthenticationPrincipal String userId,
                                        @PathVariable("id") String id) {
        taskService.deleteTask(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/history")
    public ResponseEntity<HistoryResponse> getHistory(@AuthenticationPrincipal String userId,
                                                      @RequestParam(value = "range", defaultValue = "week") String range) {
        HistoryResponse history = taskService.getHistory(userId, range);
        return ResponseEntity.ok(history);
    }
}
