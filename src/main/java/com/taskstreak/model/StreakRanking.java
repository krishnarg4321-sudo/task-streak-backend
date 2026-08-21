package com.taskstreak.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "streak_rankings")
@CompoundIndex(name = "group_week_idx", def = "{'groupId': 1, 'weekStart': 1}", unique = true)
public class StreakRanking {
    public static class RankEntry {
        private String userId;
        private String username;
        private String name;
        private String profilePictureUrl;
        private double completionRate;
        private int tasksCompleted;
        private int totalTasks;
        private int currentStreakDays;
        private double score;
        private int rank;

        public RankEntry() {}

        public RankEntry(String userId, String username, String name, String profilePictureUrl,
                         double completionRate, int tasksCompleted, int totalTasks, int currentStreakDays, double score, int rank) {
            this.userId = userId;
            this.username = username;
            this.name = name;
            this.profilePictureUrl = profilePictureUrl;
            this.completionRate = completionRate;
            this.tasksCompleted = tasksCompleted;
            this.totalTasks = totalTasks;
            this.currentStreakDays = currentStreakDays;
            this.score = score;
            this.rank = rank;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

        public int getTasksCompleted() { return tasksCompleted; }
        public void setTasksCompleted(int tasksCompleted) { this.tasksCompleted = tasksCompleted; }

        public int getTotalTasks() { return totalTasks; }
        public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

        public int getCurrentStreakDays() { return currentStreakDays; }
        public void setCurrentStreakDays(int currentStreakDays) { this.currentStreakDays = currentStreakDays; }

        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }

        public int getRank() { return rank; }
        public void setRank(int rank) { this.rank = rank; }
    }

    @Id
    private String id;
    private String groupId;
    private String weekStart; // YYYY-MM-DD (Monday)
    private List<RankEntry> ranking = new ArrayList<>();
    private Instant computedAt = Instant.now();

    public StreakRanking() {}

    public StreakRanking(String groupId, String weekStart, List<RankEntry> ranking) {
        this.groupId = groupId;
        this.weekStart = weekStart;
        this.ranking = ranking != null ? ranking : new ArrayList<>();
        this.computedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getWeekStart() { return weekStart; }
    public void setWeekStart(String weekStart) { this.weekStart = weekStart; }

    public List<RankEntry> getRanking() { return ranking; }
    public void setRanking(List<RankEntry> ranking) { this.ranking = ranking; }

    public Instant getComputedAt() { return computedAt; }
    public void setComputedAt(Instant computedAt) { this.computedAt = computedAt; }
}
