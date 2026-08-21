package com.taskstreak.controller;

import com.taskstreak.dto.SocialDtos.AddGroupMemberRequest;
import com.taskstreak.dto.SocialDtos.CreateGroupRequest;
import com.taskstreak.dto.SocialDtos.GroupProgressDto;
import com.taskstreak.model.Group;
import com.taskstreak.service.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<Group> createGroup(@AuthenticationPrincipal String userId,
                                             @RequestBody CreateGroupRequest request) {
        Group group = groupService.createGroup(userId, request);
        return ResponseEntity.ok(group);
    }

    @GetMapping
    public ResponseEntity<List<Group>> getUserGroups(@AuthenticationPrincipal String userId) {
        List<Group> groups = groupService.getUserGroups(userId);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@AuthenticationPrincipal String userId,
                                       @PathVariable("id") String groupId,
                                       @RequestBody AddGroupMemberRequest request) {
        try {
            Group group = groupService.addMember(groupId, userId, request.getUsernameOrEmail());
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<GroupProgressDto> getGroupProgress(@AuthenticationPrincipal String userId,
                                                             @PathVariable("id") String groupId) {
        GroupProgressDto progress = groupService.getGroupProgress(groupId, userId);
        return ResponseEntity.ok(progress);
    }
}
