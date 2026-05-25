package com.chatapp.controller;

import com.chatapp.config.JwtUtil;
import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final ChatService chatService;
    private final JwtUtil jwtUtil;

    private static final List<String> AVATAR_COLORS = List.of(
        "#ef4444", "#f97316", "#eab308", "#22c55e",
        "#06b6d4", "#3b82f6", "#8b5cf6", "#ec4899",
        "#14b8a6", "#f43f5e"
    );

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder,
                          AuthenticationManager authManager, ChatService chatService, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.username == null || req.username.isBlank() || req.password == null || req.password.isBlank()) {
            return badLogin("Username and password are required");
        }
        if (req.username.length() < 3 || req.username.length() > 20) {
            return badLogin("Username must be 3-20 characters");
        }
        if (req.password.length() < 6) {
            return badLogin("Password must be at least 6 characters");
        }
        if (userRepo.existsByUsername(req.username)) {
            return badLogin("Username already taken");
        }

        String color = AVATAR_COLORS.get((int)(Math.random() * AVATAR_COLORS.size()));
        User user = new User(
            req.username,
            passwordEncoder.encode(req.password),
            req.displayName != null && !req.displayName.isBlank() ? req.displayName : req.username,
            color
        );
        if (req.nickname != null) user.setNickname(req.nickname);
        if (req.email != null) user.setEmail(req.email);
        if (req.phone != null) user.setPhone(req.phone);
        if (req.linkedinUrl != null) user.setLinkedinUrl(req.linkedinUrl);
        if (req.address != null) user.setAddress(req.address);
        if (req.profilePicture != null) user.setProfilePicture(req.profilePicture);
        if (req.email != null && !req.email.isBlank()) {
            user.setProfileComplete(true);
        }
        userRepo.save(user);

        LoginResponse res = new LoginResponse();
        res.success = true;
        res.message = "Account created";
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpSession session) {
        LoginResponse res = new LoginResponse();
        try {
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password)
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            User user = userRepo.findByUsername(req.username).orElseThrow();
            user.setOnline(true);
            userRepo.save(user);

            String token = jwtUtil.generateToken(user.getUsername());

            res.success = true;
            res.username = user.getUsername();
            res.displayName = user.getDisplayName();
            res.avatarColor = user.getAvatarColor();
            res.profileComplete = user.isProfileComplete();
            res.profilePicture = user.getProfilePicture();
            res.token = token;
            res.status = user.getStatus() != null ? user.getStatus().name() : "ACTIVE";
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            res.success = false;
            res.message = "Invalid credentials";
            return ResponseEntity.status(401).body(res);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestParam String username, HttpSession session) {
        userRepo.findByUsername(username).ifPresent(u -> {
            u.setOnline(false);
            userRepo.save(u);
        });
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepo.findByUsername(auth.getName()).orElseThrow();
        LoginResponse res = new LoginResponse();
        res.success = true;
        res.username = user.getUsername();
        res.displayName = user.getDisplayName();
        res.avatarColor = user.getAvatarColor();
        res.profileComplete = user.isProfileComplete();
        res.profilePicture = user.getProfilePicture();
        res.status = user.getStatus() != null ? user.getStatus().name() : "ACTIVE";
        return ResponseEntity.ok(res);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeRequest req, Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        if (req.newPassword == null || req.newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters"));
        }
        boolean success = chatService.changePassword(auth.getName(), req.currentPassword, req.newPassword, passwordEncoder);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }
    }

    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(Authentication auth, HttpSession session) {
        if (auth == null) return ResponseEntity.status(401).build();
        chatService.deleteAccount(auth.getName());
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Account deleted"));
    }

    private ResponseEntity<?> badLogin(String msg) {
        LoginResponse res = new LoginResponse();
        res.success = false;
        res.message = msg;
        return ResponseEntity.badRequest().body(res);
    }
}
