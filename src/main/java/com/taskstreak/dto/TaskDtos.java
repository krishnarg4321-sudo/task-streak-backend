package com.taskstreak.dto;

import com.taskstreak.model.ChecklistItem;
import com.taskstreak.model.Task;
import java.util.List;
import java.util.Map;

public class TaskDtos {
    public static class CreateTaskRequest {
        private String name;
        private String description;
        private String date; // YYYY-MM-DD
        private String color;
        private String face;
        private List<ChecklistItem> checklist;

        public CreateTaskRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }

        public String getFace() { return face; }
        public void setFace(String face) { this.face = face; }

        public List<ChecklistItem> getChecklist() { return checklist; }
        public void setChecklist(List<ChecklistItem> checklist) { this.checklist = checklist; }
    }

    public static class UpdateTaskStatusRequest {
        private Task.TaskStatus status;

        public UpdateTaskStatusRequest() {}

        public Task.TaskStatus getStatus() { return status; }
        public void setStatus(Task.TaskStatus status) { this.status = status; }
    }

    public static class UpdateTaskTimerRequest {
        private String action; // start, pause, finish
        private long additionalSeconds;
        private Long totalTimeSpentSeconds;

        public UpdateTaskTimerRequest() {}

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public long getAdditionalSeconds() { return additionalSeconds; }
        public void setAdditionalSeconds(long additionalSeconds) { this.additionalSeconds = additionalSeconds; }

        public Long getTotalTimeSpentSeconds() { return totalTimeSpentSeconds; }
        public void setTotalTimeSpentSeconds(Long totalTimeSpentSeconds) { this.totalTimeSpentSeconds = totalTimeSpentSeconds; }
    }

    public static class HistoryResponse {
        private Map<String, Object> todaySummary;
        private List<Map<String, Object>> trend; // Day by day
        private List<Task> tasks;
        private double averageTimeSpentSeconds;
        private long totalTimeSpentSeconds;
        private int totalTasks;
        private int completedTasks;
        private int partiallyCompletedTasks;
        private int notCompletedTasks;

        public HistoryResponse() {}

        public Map<String, Object> getTodaySummary() { return todaySummary; }
        public void setTodaySummary(Map<String, Object> todaySummary) { this.todaySummary = todaySummary; }

        public List<Map<String, Object>> getTrend() { return trend; }
        public void setTrend(List<Map<String, Object>> trend) { this.trend = trend; }

        public List<Task> getTasks() { return tasks; }
        public void setTasks(List<Task> tasks) { this.tasks = tasks; }

        public double getAverageTimeSpentSeconds() { return averageTimeSpentSeconds; }
        public void setAverageTimeSpentSeconds(double averageTimeSpentSeconds) { this.averageTimeSpentSeconds = averageTimeSpentSeconds; }

        public long getTotalTimeSpentSeconds() { return totalTimeSpentSeconds; }
        public void setTotalTimeSpentSeconds(long totalTimeSpentSeconds) { this.totalTimeSpentSeconds = totalTimeSpentSeconds; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

        public int getPartiallyCompletedTasks() { return partiallyCompletedTasks; }
        public void setPartiallyCompletedTasks(int partiallyCompletedTasks) { this.partiallyCompletedTasks = partiallyCompletedTasks; }

        public int getNotCompletedTasks() { return notCompletedTasks; }
        public void setNotCompletedTasks(int notCompletedTasks) { this.notCompletedTasks = notCompletedTasks; }
    }
}
