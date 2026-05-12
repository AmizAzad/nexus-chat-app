package com.chatapp.controller;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(Authentication auth) {
        return ResponseEntity.ok(chatService.getAllUsers(auth.getName()));
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        // Simple search via all users filter
        return ResponseEntity.ok(chatService.getAllUsers(""));
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupDTO>> getMyGroups(Authentication auth) {
        return ResponseEntity.ok(chatService.getUserGroups(auth.getName()));
    }

    @PostMapping("/groups")
    public ResponseEntity<GroupDTO> createGroup(@RequestBody CreateGroupRequest req, Authentication auth) {
        return ResponseEntity.ok(chatService.createGroup(auth.getName(), req));
    }

    @PostMapping("/groups/{groupId}/members")
    public ResponseEntity<GroupDTO> addMember(@PathVariable Long groupId,
                                               @RequestBody AddMemberRequest req,
                                               Authentication auth) {
        return ResponseEntity.ok(chatService.addMember(groupId, req.username));
    }

    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getGroupHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatService.getGroupHistory(groupId));
    }

    @GetMapping("/dm/{targetUsername}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getDmHistory(
            @PathVariable String targetUsername, Authentication auth) {
        return ResponseEntity.ok(chatService.getDmHistory(auth.getName(), targetUsername));
    }

    // Profile endpoints
    @GetMapping("/profile/{username}")
    public ResponseEntity<UserDTO> getProfile(@PathVariable String username) {
        return ResponseEntity.ok(chatService.getProfile(username));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(@RequestBody ProfileUpdateRequest req, Authentication auth) {
        return ResponseEntity.ok(chatService.updateProfile(auth.getName(), req));
    }

    @GetMapping("/profile/me")
    public ResponseEntity<UserDTO> getMyProfile(Authentication auth) {
        return ResponseEntity.ok(chatService.getProfile(auth.getName()));
    }
}
