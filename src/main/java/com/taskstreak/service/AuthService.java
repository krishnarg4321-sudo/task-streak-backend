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
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        UserDto userDto = toUserDto(user);
        return new AuthResponse(token, userDto);
    }

    public UserDto getUserDtoById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toUserDto(user);
    }

    public UserDto updateUser(String userId, String name, String profilePictureUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (name != null && !name.isBlank()) user.setName(name);
        if (profilePictureUrl != null && !profilePictureUrl.isBlank()) user.setProfilePictureUrl(profilePictureUrl);
        user = userRepository.save(user);
        return toUserDto(user);
    }

    public UserDto toUserDto(User user) {
        UserDto dto = new UserDto(user);
        dto.setCurrentStreak(calculateStreak(user.getId()));
        dto.setOnline(true);
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
