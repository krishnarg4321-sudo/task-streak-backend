package com.taskstreak.repository;

import com.taskstreak.model.NotificationEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<NotificationEvent, String> {
    List<NotificationEvent> findByUserIdOrderByCreatedAtDesc(String userId);
    List<NotificationEvent> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);
    long countByUserIdAndReadFalse(String userId);
}
