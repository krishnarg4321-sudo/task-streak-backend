package com.taskstreak.scheduler;

import com.taskstreak.model.Group;
import com.taskstreak.model.NotificationEvent.NotificationType;
import com.taskstreak.model.User;
import com.taskstreak.repository.GroupRepository;
import com.taskstreak.repository.TaskRepository;
import com.taskstreak.repository.UserRepository;
import com.taskstreak.service.AuthService;
import com.taskstreak.service.NotificationService;
import com.taskstreak.service.StreakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DailyReminderScheduler {
    private static final Logger log = LoggerFactory.getLogger(DailyReminderScheduler.class);

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    public DailyReminderScheduler(UserRepository userRepository, TaskRepository taskRepository,
                                  NotificationService notificationService, AuthService authService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    // Daily reminder triggered at 8 PM (configurable via cron in properties)
    @Scheduled(cron = "${app.daily-reminder.cron:0 0 20 * * *}")
    public void runDailyReminderJob() {
        log.info("Running Daily Task Reminder Job...");
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        List<User> users = userRepository.findAll();

        for (User u : users) {
            boolean hasTaskToday = taskRepository.existsByUserIdAndDate(u.getId(), today);
            if (!hasTaskToday) {
                int streak = authService.calculateStreak(u.getId());
                Map<String, Object> payload = new HashMap<>();
                payload.put("name", u.getName());
                payload.put("streak", streak);
                
                notificationService.createAndSendNotification(u.getId(), NotificationType.DAILY_REMINDER, payload);
                log.info("Sent daily reminder to user: {}", u.getUsername());
            }
        }
    }
}
