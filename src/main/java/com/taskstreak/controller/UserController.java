package com.taskstreak.controller;

import com.taskstreak.dto.AuthDtos.UserDto;
import com.taskstreak.model.User;
import com.taskstreak.service.AuthService;
import com.taskstreak.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AuthService authService;
    private final FriendService friendService;

    public UserController(AuthService authService, FriendService friendService) {
        this.authService = authService;
        this.friendService = friendService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal String userId) {
        UserDto dto = authService.getUserDtoById(userId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(@AuthenticationPrincipal String userId,
                                                 @RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String avatarUrl = payload.get("profilePictureUrl");
        UserDto updated = authService.updateUser(userId, name, avatarUrl);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@AuthenticationPrincipal String userId,
                                                 @RequestParam("q") String query) {
        List<User> users = friendService.searchUsers(query, userId);
        return ResponseEntity.ok(users);
    }
}
