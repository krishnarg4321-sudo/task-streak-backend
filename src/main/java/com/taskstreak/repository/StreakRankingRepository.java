package com.taskstreak.repository;

import com.taskstreak.model.StreakRanking;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface StreakRankingRepository extends MongoRepository<StreakRanking, String> {
    Optional<StreakRanking> findByGroupIdAndWeekStart(String groupId, String weekStart);
    Optional<StreakRanking> findTopByGroupIdOrderByWeekStartDesc(String groupId);
}
