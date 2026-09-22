package com.taskstreak.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskstreak.model.PushSubscription;
import com.taskstreak.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Utils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.Security;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WebPushService {

    private static final Logger log = LoggerFactory.getLogger(WebPushService.class);

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;

    @Value("${vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${vapid.subject:mailto:admin@taskstreak.com}")
    private String vapidSubject;

    private PushService pushService;

    public WebPushService(PushSubscriptionRepository pushSubscriptionRepository, ObjectMapper objectMapper) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        try {
            if (vapidPublicKey == null || vapidPublicKey.isBlank() || vapidPrivateKey == null || vapidPrivateKey.isBlank()) {
                log.warn("[WebPush] VAPID keys not configured in environment. Generating dynamic fallback keypair...");
                java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
                java.security.spec.ECGenParameterSpec ecSpec = new java.security.spec.ECGenParameterSpec("prime256v1");
                kpg.initialize(ecSpec);
                KeyPair keyPair = kpg.generateKeyPair();
                vapidPublicKey = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(Utils.encode((org.bouncycastle.jce.interfaces.ECPublicKey) keyPair.getPublic()));
                vapidPrivateKey = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(Utils.encode((org.bouncycastle.jce.interfaces.ECPrivateKey) keyPair.getPrivate()));
                log.info("[WebPush] Generated VAPID Public Key: {}", vapidPublicKey);
            }

            pushService = new PushService(vapidPublicKey, vapidPrivateKey, vapidSubject);
            log.info("[WebPush] WebPushService initialized successfully with subject: {}", vapidSubject);
        } catch (Exception e) {
            log.error("[WebPush] Failed to initialize PushService: {}", e.getMessage(), e);
        }
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    public PushSubscription subscribe(String userId, String endpoint, String p256dh, String auth, String userAgent) {
        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(endpoint);
        PushSubscription subscription;
        if (existing.isPresent()) {
            subscription = existing.get();
            subscription.setUserId(userId);
            subscription.setP256dh(p256dh);
            subscription.setAuth(auth);
            subscription.setUserAgent(userAgent);
            subscription.setUpdatedAt(Instant.now());
        } else {
            subscription = new PushSubscription(userId, endpoint, p256dh, auth, userAgent);
        }
        return pushSubscriptionRepository.save(subscription);
    }

    public void unsubscribe(String endpoint) {
        pushSubscriptionRepository.deleteByEndpoint(endpoint);
    }

    @Async
    public void sendPush(String userId, String title, String body, Map<String, Object> extraData) {
        if (pushService == null) {
            log.warn("[WebPush] PushService is not initialized. Skipping push notification.");
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            log.debug("[WebPush] No push subscriptions found for userId: {}", userId);
            return;
        }

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("title", title);
        payloadMap.put("body", body);
        payloadMap.put("message", body);
        payloadMap.put("icon", "/icons/icon-192.svg");
        payloadMap.put("badge", "/icons/icon-192.svg");
        payloadMap.put("data", extraData != null ? extraData : new HashMap<>());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payloadMap);
        } catch (Exception e) {
            log.error("[WebPush] Failed to serialize push payload: {}", e.getMessage());
            return;
        }

        for (PushSubscription sub : subscriptions) {
            sendToSubscription(sub, payloadJson);
        }
    }

    public boolean sendDirectPush(PushSubscription sub, String title, String body, Map<String, Object> extraData) {
        if (pushService == null) return false;

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("title", title);
        payloadMap.put("body", body);
        payloadMap.put("message", body);
        payloadMap.put("icon", "/icons/icon-192.svg");
        payloadMap.put("badge", "/icons/icon-192.svg");
        payloadMap.put("data", extraData != null ? extraData : new HashMap<>());

        try {
            String payloadJson = objectMapper.writeValueAsString(payloadMap);
            return sendToSubscription(sub, payloadJson);
        } catch (Exception e) {
            log.error("[WebPush] Error sending direct push: {}", e.getMessage());
            return false;
        }
    }

    private boolean sendToSubscription(PushSubscription sub, String payloadJson) {
        try {
            Notification notification = new Notification(
                    sub.getEndpoint(),
                    sub.getP256dh(),
                    sub.getAuth(),
                    payloadJson.getBytes(StandardCharsets.UTF_8)
            );

            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                log.info("[WebPush] Notification delivered successfully to endpoint (status: {})", statusCode);
                return true;
            } else if (statusCode == 404 || statusCode == 410) {
                log.warn("[WebPush] Subscription expired or gone (status: {}). Removing endpoint: {}", statusCode, sub.getEndpoint());
                pushSubscriptionRepository.delete(sub);
            } else {
                log.warn("[WebPush] Unexpected HTTP status {} when sending push notification", statusCode);
            }
        } catch (Exception e) {
            log.error("[WebPush] Failed to send push to endpoint {}: {}", sub.getEndpoint(), e.getMessage());
        }
        return false;
    }
}
