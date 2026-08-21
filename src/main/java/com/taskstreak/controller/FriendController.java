package com.taskstreak.controller;

import com.taskstreak.dto.SocialDtos.AcceptFriendRequestDto;
import com.taskstreak.dto.SocialDtos.FriendRequestDto;
import com.taskstreak.dto.SocialDtos.FriendResponseDto;
import com.taskstreak.model.Friendship;
import com.taskstreak.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendFriendRequest(@AuthenticationPrincipal String userId,
                                               @RequestBody FriendRequestDto request) {
        try {
            Friendship friendship = friendService.sendFriendRequest(userId, request.getUsernameOrEmail());
            return ResponseEntity.ok(friendship);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptFriendRequest(@AuthenticationPrincipal String userId,
                                                 @RequestBody AcceptFriendRequestDto request) {
        try {
            Friendship friendship = friendService.acceptFriendRequest(userId, request.getFriendshipId());
            return ResponseEntity.ok(friendship);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<FriendResponseDto>> getFriends(@AuthenticationPrincipal String userId) {
        List<FriendResponseDto> friends = friendService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<Map<String, Object>> getFriendProgress(@AuthenticationPrincipal String userId,
                                                                 @PathVariable("id") String friendId) {
        Map<String, Object> progress = friendService.getFriendProgress(userId, friendId);
        return ResponseEntity.ok(progress);
    }
}
