package com.taskstreak.dto;

import com.taskstreak.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public static class SignupRequest {
        @NotBlank
        private String name;

        @NotBlank
        @Size(min = 3, max = 30)
        private String username;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 6)
        private String password;

        private String profilePictureUrl;

        public SignupRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    }

    public static class LoginRequest {
        @NotBlank
        private String emailOrUsername;

        @NotBlank
        private String password;

        public LoginRequest() {}

        public String getEmailOrUsername() { return emailOrUsername; }
        public void setEmailOrUsername(String emailOrUsername) { this.emailOrUsername = emailOrUsername; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UserDto {
        private String id;
        private String name;
        private String username;
        private String email;
        private String profilePictureUrl;
        private int currentStreak;
        private boolean online;

        public UserDto() {}

        public UserDto(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.profilePictureUrl = user.getProfilePictureUrl();
            this.online = true;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

        public int getCurrentStreak() { return currentStreak; }
        public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
    }

    public static class AuthResponse {
        private String token;
        private UserDto user;

        public AuthResponse() {}

        public AuthResponse(String token, UserDto user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public UserDto getUser() { return user; }
        public void setUser(UserDto user) { this.user = user; }
    }
}
