package com.chatapp.controller;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invite")
public class InviteController {

    private final ChatService chatService;

    public InviteController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/{token}")
    public ResponseEntity<?> getInviteInfo(@PathVariable String token) {
        try {
            // Just validate the token exists and is valid
            // This endpoint is public so users can see what group they're joining
            return ResponseEntity.ok(Map.of("valid", true, "token", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{token}/accept")
    public ResponseEntity<?> acceptInvite(@PathVariable String token, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        try {
            GroupDTO group = chatService.acceptInvite(token, auth.getName());
            return ResponseEntity.ok(group);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

