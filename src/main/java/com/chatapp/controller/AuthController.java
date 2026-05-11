package com.chatapp.controller;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    private static final List<String> AVATAR_COLORS = List.of(
        "#ef4444", "#f97316", "#eab308", "#22c55e",
        "#06b6d4", "#3b82f6", "#8b5cf6", "#ec4899",
        "#14b8a6", "#f43f5e"
    );

    public AuthController(UserRepository userRepo, PasswordEncoder passwordEncoder,
                          AuthenticationManager authManager) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (userRepo.existsByUsername(req.username)) {
            LoginResponse res = new LoginResponse();
            res.success = false;
            res.message = "Username already taken";
            return ResponseEntity.badRequest().body(res);
        }

        String color = AVATAR_COLORS.get((int)(Math.random() * AVATAR_COLORS.size()));
        User user = new User(
            req.username,
            passwordEncoder.encode(req.password),
            req.displayName != null && !req.displayName.isBlank() ? req.displayName : req.username,
            color
        );
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

            res.success = true;
            res.username = user.getUsername();
            res.displayName = user.getDisplayName();
            res.avatarColor = user.getAvatarColor();
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
        return ResponseEntity.ok(res);
    }
}
