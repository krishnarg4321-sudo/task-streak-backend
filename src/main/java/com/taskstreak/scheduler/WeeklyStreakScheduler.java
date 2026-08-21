package com.taskstreak.scheduler;

import com.taskstreak.model.Group;
import com.taskstreak.repository.GroupRepository;
import com.taskstreak.service.StreakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WeeklyStreakScheduler {
    private static final Logger log = LoggerFactory.getLogger(WeeklyStreakScheduler.class);

    private final GroupRepository groupRepository;
    private final StreakService streakService;

    public WeeklyStreakScheduler(GroupRepository groupRepository, StreakService streakService) {
        this.groupRepository = groupRepository;
        this.streakService = streakService;
    }

    // Weekly streak reset & ranking computation (Mondays midnight)
    @Scheduled(cron = "${app.weekly-streak.cron:0 0 0 * * MON}")
    public void computeWeeklyStreaks() {
        log.info("Running Weekly Streak Ranking Computation Job...");
        List<Group> groups = groupRepository.findAll();
        for (Group group : groups) {
            try {
                streakService.computeAndSaveWeeklyRanking(group.getId());
                log.info("Computed weekly ranking for group: {}", group.getName());
            } catch (Exception e) {
                log.error("Failed to compute weekly ranking for group: {}", group.getId(), e);
            }
        }
    }
}
