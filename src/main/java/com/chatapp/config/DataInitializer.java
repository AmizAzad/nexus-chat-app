package com.chatapp.config;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedData(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            if (!userRepo.existsByUsername("alice")) {
                userRepo.save(new User("alice", encoder.encode("password"), "Alice Chen", "#ef4444"));
            }
            if (!userRepo.existsByUsername("bob")) {
                userRepo.save(new User("bob", encoder.encode("password"), "Bob Kumar", "#3b82f6"));
            }
            if (!userRepo.existsByUsername("carol")) {
                userRepo.save(new User("carol", encoder.encode("password"), "Carol Smith", "#22c55e"));
            }
            if (!userRepo.existsByUsername("dave")) {
                userRepo.save(new User("dave", encoder.encode("password"), "Dave Patel", "#f97316"));
            }
        };
    }
}
