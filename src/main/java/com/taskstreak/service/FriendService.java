package com.taskstreak.service;

import com.taskstreak.dto.SocialDtos.FriendResponseDto;
import com.taskstreak.model.Friendship;
import com.taskstreak.model.NotificationEvent.NotificationType;
import com.taskstreak.model.Task;
import com.taskstreak.model.User;
import com.taskstreak.repository.FriendshipRepository;
import com.taskstreak.repository.TaskRepository;
import com.taskstreak.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public FriendService(FriendshipRepository friendshipRepository, UserRepository userRepository,
                         TaskRepository taskRepository, AuthService authService,
                         NotificationService notificationService) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    public Friendship sendFriendRequest(String currentUserId, String identifier) {
        String cleanId = identifier.trim().toLowerCase();
        User target = userRepository.findByUsername(cleanId)
                .or(() -> userRepository.findByEmail(cleanId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));

        if (target.getId().equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot add yourself as a friend");
        }

        Optional<Friendship> existing = friendshipRepository.findByUserIdAndFriendId(currentUserId, target.getId())
                .or(() -> friendshipRepository.findByUserIdAndFriendId(target.getId(), currentUserId));

        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == Friendship.Status.ACCEPTED) {
                throw new IllegalArgumentException("You are already friends with " + target.getName());
            } else if (f.getStatus() == Friendship.Status.PENDING) {
                if (f.getFriendId().equals(currentUserId)) {
                    // Auto accept if incoming request already exists
                    f.setStatus(Friendship.Status.ACCEPTED);
                    return friendshipRepository.save(f);
                }
                throw new IllegalArgumentException("Friend request already pending");
            }
        }

        Friendship friendship = new Friendship(currentUserId, target.getId(), Friendship.Status.PENDING);
        Friendship saved = friendshipRepository.save(friendship);

        // Notify target
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("friendName", currentUser.getName());
            notificationService.createAndSendNotification(target.getId(), NotificationType.FRIEND_REQUEST, payload);
        }

        return saved;
    }

    public Friendship acceptFriendRequest(String currentUserId, String friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!friendship.getFriendId().equals(currentUserId)) {
            throw new IllegalArgumentException("You cannot accept this friend request");
        }

        friendship.setStatus(Friendship.Status.ACCEPTED);
        return friendshipRepository.save(friendship);
    }

    public List<FriendResponseDto> getFriends(String currentUserId) {
        List<Friendship> allFriendships = friendshipRepository.findByUserIdOrFriendId(currentUserId, currentUserId);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<FriendResponseDto> result = new ArrayList<>();
        for (Friendship f : allFriendships) {
            String otherUserId = f.getUserId().equals(currentUserId) ? f.getFriendId() : f.getUserId();
            User otherUser = userRepository.findById(otherUserId).orElse(null);
            if (otherUser != null) {
                FriendResponseDto dto = new FriendResponseDto();
                dto.setFriendshipId(f.getId());
                dto.setUserId(otherUser.getId());
                dto.setUsername(otherUser.getUsername());
                dto.setName(otherUser.getName());
                dto.setProfilePictureUrl(otherUser.getProfilePictureUrl());
                dto.setStatus(f.getStatus());
                dto.setCurrentStreak(authService.calculateStreak(otherUser.getId()));
                dto.setOnline(true);

                List<Task> todayTasks = taskRepository.findByUserIdAndDate(otherUser.getId(), today);
                dto.setTodayTasksCount(todayTasks.size());
                dto.setTodayCompletedCount((int) todayTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count());

                result.add(dto);
            }
        }
        return result;
    }

    public Map<String, Object> getFriendProgress(String currentUserId, String friendId) {
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("Friend not found"));

        int streak = authService.calculateStreak(friendId);
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<Task> todayTasks = taskRepository.findByUserIdAndDate(friendId, today);

        // 7-day trend
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        List<Task> weekTasks = taskRepository.findByUserIdAndDateBetween(friendId, start.format(fmt), end.format(fmt));

        Map<String, Object> res = new HashMap<>();
        res.put("friend", authService.toUserDto(friend));
        res.put("streak", streak);
        res.put("todayTasks", todayTasks);
        res.put("weeklyTotal", weekTasks.size());
        res.put("weeklyCompleted", weekTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count());
        return res;
    }

    public List<User> searchUsers(String query, String currentUserId) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(query.trim(), query.trim())
                .stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .limit(10)
                .collect(Collectors.toList());
    }
}
