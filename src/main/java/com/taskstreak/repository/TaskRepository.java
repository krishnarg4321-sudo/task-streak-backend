package com.taskstreak.repository;

import com.taskstreak.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByUserIdAndDate(String userId, String date);
    List<Task> findByUserIdAndDateBetween(String userId, String startDate, String endDate);
    List<Task> findByUserId(String userId);
    List<Task> findByUserIdInAndDateBetween(List<String> userIds, String startDate, String endDate);
    long countByUserIdAndDate(String userId, String date);
    boolean existsByUserIdAndDate(String userId, String date);
}
