package com.taskstreak.controller;

import com.taskstreak.model.StreakRanking;
import com.taskstreak.service.StreakService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/streaks")
public class StreakController {
    private final StreakService streakService;

    public StreakController(StreakService streakService) {
        this.streakService = streakService;
    }

    @GetMapping("/{groupId}/weekly")
    public ResponseEntity<StreakRanking> getWeeklyStreakRanking(@AuthenticationPrincipal String userId,
                                                                @PathVariable("groupId") String groupId) {
        StreakRanking ranking = streakService.getWeeklyRanking(groupId);
        return ResponseEntity.ok(ranking);
    }
}
