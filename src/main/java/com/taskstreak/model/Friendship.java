package com.taskstreak.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "friendships")
@CompoundIndex(name = "user_friend_idx", def = "{'userId': 1, 'friendId': 1}", unique = true)
public class Friendship {
    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    @Id
    private String id;
    private String userId;
    private String friendId;
    private Status status = Status.PENDING;
    private Instant createdAt = Instant.now();

    public Friendship() {}

    public Friendship(String userId, String friendId, Status status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFriendId() { return friendId; }
    public void setFriendId(String friendId) { this.friendId = friendId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
