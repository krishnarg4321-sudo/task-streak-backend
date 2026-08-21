package com.taskstreak.controller;

import com.taskstreak.model.NotificationEvent;
import com.taskstreak.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(@AuthenticationPrincipal String userId) {
        List<NotificationEvent> list = notificationService.getNotificationsForUser(userId);
        long unreadCount = notificationService.getUnreadCount(userId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("notifications", list);
        resp.put("unreadCount", unreadCount);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/read")
    public ResponseEntity<?> markAsRead(@AuthenticationPrincipal String userId,
                                        @RequestBody(required = false) Map<String, String> payload) {
        if (payload != null && payload.containsKey("id")) {
            NotificationEvent n = notificationService.markAsRead(payload.get("id"), userId);
            return ResponseEntity.ok(n);
        } else {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        }
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markSpecificAsRead(@AuthenticationPrincipal String userId,
                                                @PathVariable("id") String id) {
        NotificationEvent n = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(n);
    }
}
