package com.taskstreak.service;

import com.taskstreak.dto.SocialDtos.CreateGroupRequest;
import com.taskstreak.dto.SocialDtos.GroupProgressDto;
import com.taskstreak.dto.SocialDtos.MemberProgressDto;
import com.taskstreak.model.Group;
import com.taskstreak.model.Task;
import com.taskstreak.model.User;
import com.taskstreak.repository.GroupRepository;
import com.taskstreak.repository.TaskRepository;
import com.taskstreak.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;

    public GroupService(GroupRepository groupRepository, UserRepository userRepository,
                        TaskRepository taskRepository, AuthService authService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
    }

    public Group createGroup(String ownerId, CreateGroupRequest request) {
        List<String> members = new ArrayList<>();
        members.add(ownerId);

        Group group = new Group(
                request.getName() != null && !request.getName().isBlank() ? request.getName() : "Task Squad",
                request.getDescription() != null ? request.getDescription() : "Let's build daily streaks together!",
                ownerId,
                members
        );
        return groupRepository.save(group);
    }

    public List<Group> getUserGroups(String userId) {
        List<Group> groups = groupRepository.findByMemberIdsContaining(userId);
        if (groups.isEmpty()) {
            // Create a default group for the user if none exist
            CreateGroupRequest defaultReq = new CreateGroupRequest();
            defaultReq.setName("Productivity Champions");
            defaultReq.setDescription("Our daily task streak and pomodoro focus circle.");
            Group defaultGroup = createGroup(userId, defaultReq);
            groups = List.of(defaultGroup);
        }
        return groups;
    }

    public Group addMember(String groupId, String currentUserId, String identifier) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        String cleanId = identifier.trim().toLowerCase();
        User target = userRepository.findByUsername(cleanId)
                .or(() -> userRepository.findByEmail(cleanId))
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + identifier));

        if (!group.getMemberIds().contains(target.getId())) {
            group.getMemberIds().add(target.getId());
            group = groupRepository.save(group);
        }

        return group;
    }

    public GroupProgressDto getGroupProgress(String groupId, String currentUserId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        List<String> memberIds = group.getMemberIds();
        List<User> members = userRepository.findAllById(memberIds);
        Map<String, User> userMap = members.stream().collect(Collectors.toMap(User::getId, u -> u));

        List<Task> groupTasks = taskRepository.findByUserIdInAndDateBetween(memberIds, startDate.format(fmt), endDate.format(fmt));
        Map<String, Map<String, List<Task>>> tasksByUserAndDate = new HashMap<>();

        for (Task t : groupTasks) {
            tasksByUserAndDate
                    .computeIfAbsent(t.getUserId(), k -> new HashMap<>())
                    .computeIfAbsent(t.getDate(), k -> new ArrayList<>())
                    .add(t);
        }

        List<MemberProgressDto> memberStats = new ArrayList<>();
        for (User u : members) {
            MemberProgressDto dto = new MemberProgressDto();
            dto.setUserId(u.getId());
            dto.setUsername(u.getUsername());
            dto.setName(u.getName());
            dto.setProfilePictureUrl(u.getProfilePictureUrl());
            dto.setCurrentStreak(authService.calculateStreak(u.getId()));
            dto.setOnline(true);

            List<Task> userWeekTasks = groupTasks.stream().filter(t -> t.getUserId().equals(u.getId())).toList();
            int completed = (int) userWeekTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
            int partial = (int) userWeekTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.PARTIALLY_COMPLETED).count();
            int total = userWeekTasks.size();
            double rate = total > 0 ? ((completed + 0.5 * partial) / total) * 100.0 : 0.0;

            dto.setWeeklyCompletedTasks(completed);
            dto.setWeeklyTotalTasks(total);
            dto.setWeeklyCompletionRate(Math.round(rate * 10.0) / 10.0);
            memberStats.add(dto);
        }

        // Daily progression for multi-line diverging chart
        List<Map<String, Object>> dailyProgression = new ArrayList<>();
        LocalDate cur = startDate;
        while (!cur.isAfter(endDate)) {
            String dateStr = cur.format(fmt);
            Map<String, Object> dayEntry = new HashMap<>();
            dayEntry.put("date", dateStr);
            dayEntry.put("day", cur.getDayOfWeek().name().substring(0, 3));

            for (User u : members) {
                List<Task> userDayTasks = tasksByUserAndDate
                        .getOrDefault(u.getId(), Collections.emptyMap())
                        .getOrDefault(dateStr, Collections.emptyList());

                long done = userDayTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
                long part = userDayTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.PARTIALLY_COMPLETED).count();
                long tot = userDayTasks.size();
                double dayScore = tot > 0 ? ((done + 0.5 * part) / tot) * 100.0 : 0.0;

                dayEntry.put(u.getUsername(), Math.round(dayScore * 10.0) / 10.0);
            }
            dailyProgression.add(dayEntry);
            cur = cur.plusDays(1);
        }

        GroupProgressDto res = new GroupProgressDto();
        res.setGroupId(group.getId());
        res.setGroupName(group.getName());
        res.setDescription(group.getDescription());
        res.setMemberCount(members.size());
        res.setMembers(memberStats);
        res.setDailyProgression(dailyProgression);
        return res;
    }
}
