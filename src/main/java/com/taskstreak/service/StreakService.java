package com.taskstreak.service;

import com.taskstreak.model.Group;
import com.taskstreak.model.StreakRanking;
import com.taskstreak.model.StreakRanking.RankEntry;
import com.taskstreak.model.Task;
import com.taskstreak.model.User;
import com.taskstreak.repository.GroupRepository;
import com.taskstreak.repository.StreakRankingRepository;
import com.taskstreak.repository.TaskRepository;
import com.taskstreak.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StreakService {
    private final StreakRankingRepository streakRankingRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;

    public StreakService(StreakRankingRepository streakRankingRepository, GroupRepository groupRepository,
                         UserRepository userRepository, TaskRepository taskRepository, AuthService authService) {
        this.streakRankingRepository = streakRankingRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
    }

    public StreakRanking computeAndSaveWeeklyRanking(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        List<String> memberIds = group.getMemberIds();
        List<User> members = userRepository.findAllById(memberIds);

        List<Task> tasks = taskRepository.findByUserIdInAndDateBetween(memberIds, monday.format(fmt), today.format(fmt));
        Map<String, List<Task>> tasksByUser = tasks.stream().collect(Collectors.groupingBy(Task::getUserId));

        List<RankEntry> entries = new ArrayList<>();
        for (User u : members) {
            List<Task> userTasks = tasksByUser.getOrDefault(u.getId(), Collections.emptyList());
            int completed = (int) userTasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
            int total = userTasks.size();
            double completionRate = total > 0 ? ((double) completed / total) * 100.0 : 0.0;
            int streakDays = authService.calculateStreak(u.getId());

            // Score formula: 60% completion rate + 25% streak consistency + 15% volume
            double score = (completionRate * 0.6) + (streakDays * 5.0) + (completed * 2.0);
            score = Math.round(score * 10.0) / 10.0;

            RankEntry entry = new RankEntry(
                    u.getId(),
                    u.getUsername(),
                    u.getName(),
                    u.getProfilePictureUrl(),
                    Math.round(completionRate * 10.0) / 10.0,
                    completed,
                    total,
                    streakDays,
                    score,
                    1
            );
            entries.add(entry);
        }

        // Sort descending by score, then completion rate, then streak
        entries.sort(Comparator.comparingDouble(RankEntry::getScore)
                .thenComparingDouble(RankEntry::getCompletionRate)
                .thenComparingInt(RankEntry::getCurrentStreakDays)
                .reversed());

        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setRank(i + 1);
        }

        StreakRanking ranking = streakRankingRepository.findByGroupIdAndWeekStart(groupId, monday.format(fmt))
                .orElse(new StreakRanking(groupId, monday.format(fmt), entries));
        ranking.setRanking(entries);
        return streakRankingRepository.save(ranking);
    }

    public StreakRanking getWeeklyRanking(String groupId) {
        LocalDate monday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        String mondayStr = monday.format(DateTimeFormatter.ISO_LOCAL_DATE);

        return streakRankingRepository.findByGroupIdAndWeekStart(groupId, mondayStr)
                .orElseGet(() -> computeAndSaveWeeklyRanking(groupId));
    }
}
