package com.taskstreak.service;

import com.taskstreak.dto.AuthDtos.AuthResponse;
import com.taskstreak.dto.AuthDtos.LoginRequest;
import com.taskstreak.dto.AuthDtos.SignupRequest;
import com.taskstreak.dto.AuthDtos.UserDto;
import com.taskstreak.config.JwtService;
import com.taskstreak.model.Task;
import com.taskstreak.model.User;
import com.taskstreak.repository.TaskRepository;
import com.taskstreak.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, TaskRepository taskRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }

        User user = new User(
                request.getName(),
                request.getUsername().toLowerCase().trim(),
                request.getEmail().toLowerCase().trim(),
                passwordEncoder.encode(request.getPassword()),
                request.getProfilePictureUrl()
        );
        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        UserDto userDto = toUserDto(user);
        return new AuthResponse(token, userDto);
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.getEmailOrUsername().toLowerCase().trim();
        Optional<User> userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }

        User user = userOpt.get();
        user.setLastActiveAt(java.time.Instant.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), user.getUsername());
        UserDto userDto = toUserDto(user);
        return new AuthResponse(token, userDto);
    }

    public UserDto getUserDtoById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setLastActiveAt(java.time.Instant.now());
        userRepository.save(user);
        return toUserDto(user);
    }

    public UserDto updateUser(String userId, String name, String profilePictureUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (name != null && !name.isBlank()) user.setName(name);
        if (profilePictureUrl != null && !profilePictureUrl.isBlank()) user.setProfilePictureUrl(profilePictureUrl);
        user.setLastActiveAt(java.time.Instant.now());
        user = userRepository.save(user);
        return toUserDto(user);
    }

    public UserDto toUserDto(User user) {
        UserDto dto = new UserDto(user);
        int streak = calculateStreak(user.getId());
        dto.setCurrentStreak(streak);

        List<Task> allUserTasks = taskRepository.findByUserId(user.getId());
        int completedTasks = (int) allUserTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
        long totalFocusSeconds = allUserTasks.stream().mapToLong(Task::getTimeSpentSeconds).sum();
        
        // Dynamic Level Calculation
        int xp = (streak * 15) + (completedTasks * 10) + (int)(totalFocusSeconds / 30);
        int level = Math.max(1, 1 + (xp / 60));
        
        String levelTitle;
        if (level <= 2) {
            levelTitle = "Novice Grinder";
        } else if (level <= 4) {
            levelTitle = "Streak Builder";
        } else if (level <= 7) {
            levelTitle = "Deep Work Specialist";
        } else if (level <= 10) {
            levelTitle = "Focus Master";
        } else {
            levelTitle = "Productivity Legend";
        }

        // Dynamic Active Status Calculation
        String activeStatus;
        java.time.Instant now = java.time.Instant.now();
        java.time.Instant lastActive = user.getLastActiveAt() != null ? user.getLastActiveAt() : user.getCreatedAt();
        long diffMinutes = java.time.Duration.between(lastActive, now).toMinutes();

        boolean hasTaskToday = taskRepository.existsByUserIdAndDate(user.getId(), LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));

        if (diffMinutes <= 10 || hasTaskToday) {
            activeStatus = "ACTIVE";
            dto.setOnline(true);
        } else if (diffMinutes <= 720) { // 12 hours
            activeStatus = "AWAY";
            dto.setOnline(true);
        } else {
            activeStatus = "OFFLINE";
            dto.setOnline(false);
        }

        dto.setLevel(level);
        dto.setLevelTitle(levelTitle);
        dto.setXp(xp);
        dto.setActiveStatus(activeStatus);
        dto.setTotalCompletedTasks(completedTasks);
        dto.setTotalFocusSeconds(totalFocusSeconds);

        return dto;
    }

    public int calculateStreak(String userId) {
        // Attendance rule: User is marked present for a day if they created >= 1 task on that day
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        int streak = 0;
        LocalDate current = today;

        // Check if user added task today; if not, check from yesterday without breaking streak yet
        boolean hasTaskToday = taskRepository.existsByUserIdAndDate(userId, current.format(fmt));
        if (hasTaskToday) {
            streak++;
            current = current.minusDays(1);
        } else {
            current = current.minusDays(1);
        }

        while (true) {
            boolean hasTask = taskRepository.existsByUserIdAndDate(userId, current.format(fmt));
            if (hasTask) {
                streak++;
                current = current.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }
}
