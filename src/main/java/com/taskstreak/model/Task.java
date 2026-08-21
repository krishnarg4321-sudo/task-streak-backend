package com.taskstreak.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "tasks")
@CompoundIndex(name = "user_date_idx", def = "{'userId': 1, 'date': 1}")
public class Task {
    public enum TaskStatus {
        NOT_COMPLETED,
        IN_PROGRESS,
        PARTIALLY_COMPLETED,
        COMPLETED
    }

    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private String name;
    private String description;
    
    @Indexed
    private String date; // YYYY-MM-DD
    
    private TaskStatus status = TaskStatus.NOT_COMPLETED;
    private long timeSpentSeconds = 0;
    
    private String color = "yellow"; // yellow, blue, pink, green, purple
    private String face = "wink";    // wink, frown, poker, focused, happy
    private List<ChecklistItem> checklist = new ArrayList<>();
    
    private Instant createdAt = Instant.now();
    private Instant completedAt;

    public Task() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public long getTimeSpentSeconds() { return timeSpentSeconds; }
    public void setTimeSpentSeconds(long timeSpentSeconds) { this.timeSpentSeconds = timeSpentSeconds; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getFace() { return face; }
    public void setFace(String face) { this.face = face; }

    public List<ChecklistItem> getChecklist() { return checklist; }
    public void setChecklist(List<ChecklistItem> checklist) { this.checklist = checklist != null ? checklist : new ArrayList<>(); }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
