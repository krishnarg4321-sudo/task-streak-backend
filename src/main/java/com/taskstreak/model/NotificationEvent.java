package com.taskstreak.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "notifications")
public class NotificationEvent {
    public enum NotificationType {
        TASK_COMPLETED,
        DAILY_REMINDER,
        FRIEND_TASK_COMPLETED,
        FRIEND_REQUEST,
        STREAK_MILESTONE
    }

    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, Object> payload = new HashMap<>();
    private boolean read = false;
    private Instant createdAt = Instant.now();

    public NotificationEvent() {}

    public NotificationEvent(String userId, NotificationType type, String title, String message, Map<String, Object> payload) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.payload = payload != null ? payload : new HashMap<>();
        this.read = false;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload != null ? payload : new HashMap<>(); }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
