package com.taskstreak.service;

import com.taskstreak.model.NotificationEvent;
import com.taskstreak.model.NotificationEvent.NotificationType;
import com.taskstreak.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // Configurable template map for custom internal notification framework
    private final Map<NotificationType, Template> templateMap = new HashMap<>();

    public static class Template {
        private final String titleTemplate;
        private final String messageTemplate;

        public Template(String titleTemplate, String messageTemplate) {
            this.titleTemplate = titleTemplate;
            this.messageTemplate = messageTemplate;
        }

        public String formatTitle(Map<String, Object> payload) {
            return interpolate(titleTemplate, payload);
        }

        public String formatMessage(Map<String, Object> payload) {
            return interpolate(messageTemplate, payload);
        }

        private String interpolate(String template, Map<String, Object> payload) {
            String result = template;
            if (payload != null) {
                for (Map.Entry<String, Object> entry : payload.entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    String val = entry.getValue() != null ? entry.getValue().toString() : "";
                    result = result.replace(placeholder, val);
                }
            }
            return result;
        }
    }

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
        initializeTemplates();
    }

    private void initializeTemplates() {
        templateMap.put(NotificationType.TASK_COMPLETED, new Template(
                "Task Completed",
                "Great work! You crushed '{taskName}' in {duration}."
        ));
        templateMap.put(NotificationType.DAILY_REMINDER, new Template(
                "Daily Task Reminder",
                "Hey {name}! You haven't added a task for today yet. Keep your {streak}-day streak alive!"
        ));
        templateMap.put(NotificationType.FRIEND_TASK_COMPLETED, new Template(
                "Friend Milestone",
                "{friendName} just completed their task '{taskName}'! Check out their progress."
        ));
        templateMap.put(NotificationType.FRIEND_REQUEST, new Template(
                "New Friend Request",
                "{friendName} wants to connect with you on Task Streak!"
        ));
        templateMap.put(NotificationType.STREAK_MILESTONE, new Template(
                "Streak Milestone Reached",
                "Awesome! You hit a {streak}-day streak milestone in {groupName}!"
        ));
    }

    public void registerCustomTemplate(NotificationType type, String titlePattern, String messagePattern) {
        templateMap.put(type, new Template(titlePattern, messagePattern));
    }

    public NotificationEvent createAndSendNotification(String userId, NotificationType type, Map<String, Object> payload) {
        Template template = templateMap.getOrDefault(type, new Template(
                type.name().replace('_', ' '),
                "You have a new update."
        ));

        String title = template.formatTitle(payload);
        String message = template.formatMessage(payload);

        NotificationEvent event = new NotificationEvent(userId, type, title, message, payload);
        return notificationRepository.save(event);
    }

    public List<NotificationEvent> getNotificationsForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public NotificationEvent markAsRead(String notificationId, String userId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUserId().equals(userId))
                .map(n -> {
                    n.setRead(true);
                    return notificationRepository.save(n);
                })
                .orElse(null);
    }

    public void markAllAsRead(String userId) {
        List<NotificationEvent> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
