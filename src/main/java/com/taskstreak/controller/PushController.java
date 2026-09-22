package com.taskstreak.controller;

import com.taskstreak.model.PushSubscription;
import com.taskstreak.service.WebPushService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final WebPushService webPushService;

    public PushController(WebPushService webPushService) {
        this.webPushService = webPushService;
    }

    public static class PushSubscriptionDto {
        private String endpoint;
        private KeysDto keys;
        private String p256dh;
        private String auth;
        private String userAgent;

        public static class KeysDto {
            private String p256dh;
            private String auth;

            public String getP256dh() { return p256dh; }
            public void setP256dh(String p256dh) { this.p256dh = p256dh; }
            public String getAuth() { return auth; }
            public void setAuth(String auth) { this.auth = auth; }
        }

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public KeysDto getKeys() { return keys; }
        public void setKeys(KeysDto keys) { this.keys = keys; }
        public String getP256dh() { return p256dh; }
        public void setP256dh(String p256dh) { this.p256dh = p256dh; }
        public String getAuth() { return auth; }
        public void setAuth(String auth) { this.auth = auth; }
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    }

    @GetMapping("/public-key")
    public ResponseEntity<Map<String, String>> getPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", webPushService.getVapidPublicKey()));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @AuthenticationPrincipal String userId,
            @RequestHeader(value = "User-Agent", required = false) String userAgentHeader,
            @RequestBody PushSubscriptionDto dto) {

        if (dto.getEndpoint() == null || dto.getEndpoint().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Endpoint is required"));
        }

        String p256dh = dto.getP256dh();
        String auth = dto.getAuth();

        if (dto.getKeys() != null) {
            if (p256dh == null || p256dh.isBlank()) {
                p256dh = dto.getKeys().getP256dh();
            }
            if (auth == null || auth.isBlank()) {
                auth = dto.getKeys().getAuth();
            }
        }

        if (p256dh == null || auth == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "p256dh and auth keys are required"));
        }

        String userAgent = dto.getUserAgent() != null ? dto.getUserAgent() : userAgentHeader;
        PushSubscription saved = webPushService.subscribe(userId, dto.getEndpoint(), p256dh, auth, userAgent);

        return ResponseEntity.ok(Map.of(
                "message", "Push subscription registered successfully",
                "subscriptionId", saved.getId()
        ));
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody Map<String, String> payload) {
        String endpoint = payload.get("endpoint");
        if (endpoint != null && !endpoint.isBlank()) {
            webPushService.unsubscribe(endpoint);
        }
        return ResponseEntity.ok(Map.of("message", "Unsubscribed from push notifications"));
    }

    @PostMapping("/test")
    public ResponseEntity<?> testPush(
            @AuthenticationPrincipal String userId,
            @RequestBody(required = false) Map<String, Object> payload) {

        String title = payload != null && payload.containsKey("title") 
                ? payload.get("title").toString() 
                : "⚡ Task Streak Push Test";
        String message = payload != null && payload.containsKey("message") 
                ? payload.get("message").toString() 
                : "Real Web Push is fully active on your device!";

        Map<String, Object> data = new HashMap<>();
        data.put("type", "TEST_PUSH");
        data.put("timestamp", System.currentTimeMillis());

        webPushService.sendPush(userId, title, message, data);

        return ResponseEntity.ok(Map.of(
                "message", "Test push notification queued for user",
                "userId", userId
        ));
    }
}
