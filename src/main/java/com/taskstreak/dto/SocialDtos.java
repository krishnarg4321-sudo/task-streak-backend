package com.taskstreak.dto;

import com.taskstreak.model.Friendship;
import java.util.List;
import java.util.Map;

public class SocialDtos {
    public static class FriendRequestDto {
        private String usernameOrEmail;

        public FriendRequestDto() {}

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
    }

    public static class AcceptFriendRequestDto {
        private String friendshipId;

        public AcceptFriendRequestDto() {}

        public String getFriendshipId() { return friendshipId; }
        public void setFriendshipId(String friendshipId) { this.friendshipId = friendshipId; }
    }

    public static class FriendResponseDto {
        private String friendshipId;
        private String userId;
        private String username;
        private String name;
        private String profilePictureUrl;
        private Friendship.Status status;
        private boolean online;
        private int currentStreak;
        private int todayTasksCount;
        private int todayCompletedCount;

        public FriendResponseDto() {}

        public String getFriendshipId() { return friendshipId; }
        public void setFriendshipId(String friendshipId) { this.friendshipId = friendshipId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

        public Friendship.Status getStatus() { return status; }
        public void setStatus(Friendship.Status status) { this.status = status; }

        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }

        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

        public int getTodayTasksCount() { return todayTasksCount; }
        public void setTodayTasksCount(int todayTasksCount) { this.todayTasksCount = todayTasksCount; }

        public int getTodayCompletedCount() { return todayCompletedCount; }
        public void setTodayCompletedCount(int todayCompletedCount) { this.todayCompletedCount = todayCompletedCount; }
    }

    public static class CreateGroupRequest {
        private String name;
        private String description;

        public CreateGroupRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class AddGroupMemberRequest {
        private String usernameOrEmail;

        public AddGroupMemberRequest() {}

        public String getUsernameOrEmail() { return usernameOrEmail; }
        public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }
    }

    public static class GroupProgressDto {
        private String groupId;
        private String groupName;
        private String description;
        private int memberCount;
        private List<MemberProgressDto> members;
        private List<Map<String, Object>> dailyProgression; // [{date: "2026-08-15", user1: 100, user2: 75...}]

        public GroupProgressDto() {}

        public String getGroupId() { return groupId; }
        public void setGroupId(String groupId) { this.groupId = groupId; }

        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

        public List<MemberProgressDto> getMembers() { return members; }
        public void setMembers(List<MemberProgressDto> members) { this.members = members; }

        public List<Map<String, Object>> getDailyProgression() { return dailyProgression; }
        public void setDailyProgression(List<Map<String, Object>> dailyProgression) { this.dailyProgression = dailyProgression; }
    }

    public static class MemberProgressDto {
        private String userId;
        private String username;
        private String name;
        private String profilePictureUrl;
        private int currentStreak;
        private double weeklyCompletionRate;
        private int weeklyCompletedTasks;
        private int weeklyTotalTasks;
        private boolean online;

        public MemberProgressDto() {}

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

        public double getWeeklyCompletionRate() { return weeklyCompletionRate; }
        public void setWeeklyCompletionRate(double weeklyCompletionRate) { this.weeklyCompletionRate = weeklyCompletionRate; }

        public int getWeeklyCompletedTasks() { return weeklyCompletedTasks; }
        public void setWeeklyCompletedTasks(int weeklyCompletedTasks) { this.weeklyCompletedTasks = weeklyCompletedTasks; }

        public int getWeeklyTotalTasks() { return weeklyTotalTasks; }
        public void setWeeklyTotalTasks(int weeklyTotalTasks) { this.weeklyTotalTasks = weeklyTotalTasks; }

        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }
}
