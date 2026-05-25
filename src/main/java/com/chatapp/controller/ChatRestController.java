package com.chatapp.controller;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    // --- Users ---
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(Authentication auth) {
        return ResponseEntity.ok(chatService.getAllUsers(auth.getName()));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserDTO>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(chatService.getAllUsers(""));
    }

    // --- Groups ---
    @GetMapping("/groups")
    public ResponseEntity<List<GroupDTO>> getMyGroups(Authentication auth) {
        return ResponseEntity.ok(chatService.getUserGroups(auth.getName()));
    }

    @GetMapping("/groups/public")
    public ResponseEntity<List<GroupDTO>> getPublicGroups() {
        return ResponseEntity.ok(chatService.getPublicGroups());
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

    @DeleteMapping("/groups/{groupId}/members/{username}")
    public ResponseEntity<GroupDTO> removeMember(@PathVariable Long groupId,
                                                  @PathVariable String username,
                                                  Authentication auth) {
        return ResponseEntity.ok(chatService.removeMember(groupId, username, auth.getName()));
    }

    @PostMapping("/groups/{groupId}/leave")
    public ResponseEntity<GroupDTO> leaveGroup(@PathVariable Long groupId, Authentication auth) {
        return ResponseEntity.ok(chatService.removeMember(groupId, auth.getName(), auth.getName()));
    }

    @PostMapping("/groups/{groupId}/join")
    public ResponseEntity<GroupDTO> joinPublicGroup(@PathVariable Long groupId, Authentication auth) {
        return ResponseEntity.ok(chatService.joinPublicGroup(groupId, auth.getName()));
    }

    @PutMapping("/groups/{groupId}/settings")
    public ResponseEntity<GroupDTO> updateGroupSettings(@PathVariable Long groupId,
                                                         @RequestBody GroupSettingsRequest req,
                                                         Authentication auth) {
        return ResponseEntity.ok(chatService.updateGroupSettings(groupId, auth.getName(), req));
    }

    @PostMapping("/groups/{groupId}/admins/{username}")
    public ResponseEntity<GroupDTO> promoteToAdmin(@PathVariable Long groupId,
                                                    @PathVariable String username,
                                                    Authentication auth) {
        return ResponseEntity.ok(chatService.promoteToAdmin(groupId, username, auth.getName()));
    }

    // --- Messages ---
    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getGroupHistory(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatService.getGroupHistory(groupId));
    }

    @GetMapping("/dm/{targetUsername}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getDmHistory(
            @PathVariable String targetUsername, Authentication auth) {
        return ResponseEntity.ok(chatService.getDmHistory(auth.getName(), targetUsername));
    }

    @GetMapping("/groups/{groupId}/pinned")
    public ResponseEntity<List<ChatMessageResponse>> getPinnedMessages(@PathVariable Long groupId) {
        return ResponseEntity.ok(chatService.getPinnedMessages(groupId));
    }

    // --- Search ---
    @GetMapping("/search")
    public ResponseEntity<SearchResult> searchMessages(@RequestParam String q) {
        return ResponseEntity.ok(chatService.searchMessages(q));
    }

    // --- Profile ---
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

    // --- Status ---
    @PutMapping("/status")
    public ResponseEntity<UserDTO> updateStatus(@RequestBody StatusUpdateRequest req, Authentication auth) {
        return ResponseEntity.ok(chatService.updateStatus(auth.getName(), req));
    }

    // --- Invites ---
    @PostMapping("/groups/{groupId}/invite")
    public ResponseEntity<InviteResponse> createInvite(@PathVariable Long groupId,
                                                        @RequestBody InviteRequest req,
                                                        Authentication auth) {
        return ResponseEntity.ok(chatService.createInvite(groupId, auth.getName(), req));
    }

    // --- Unread ---
    @GetMapping("/unread")
    public ResponseEntity<Map<String, Long>> getUnreadCounts(Authentication auth) {
        return ResponseEntity.ok(chatService.getUnreadCounts(auth.getName()));
    }

    @PostMapping("/read/{channelKey}")
    public ResponseEntity<?> markRead(@PathVariable String channelKey, Authentication auth) {
        chatService.markRead(auth.getName(), channelKey);
        return ResponseEntity.ok().build();
    }

    // --- AI ---
    @PostMapping("/groups/{groupId}/summarize")
    public ResponseEntity<Map<String, String>> summarizeGroup(@PathVariable Long groupId) {
        String summary = chatService.summarizeGroup(groupId);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    // --- Stats ---
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(chatService.getStats());
    }
}
