package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_reactions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"message_id", "user_id", "emoji"})
})
public class MessageReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String emoji;

    private LocalDateTime createdAt = LocalDateTime.now();

    public MessageReaction() {}

    public MessageReaction(Message message, User user, String emoji) {
        this.message = message;
        this.user = user;
        this.emoji = emoji;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

