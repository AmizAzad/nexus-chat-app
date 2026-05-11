package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    // For group messages
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    // For DMs: store the two participants as sorted usernames
    private String dmChannel; // e.g. "alice_bob" (alphabetically sorted)

    private LocalDateTime timestamp;
    private boolean botMessage = false;

    public Message() {}

    public Message(String content, User sender, Group group, String dmChannel) {
        this.content = content;
        this.sender = sender;
        this.group = group;
        this.dmChannel = dmChannel;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    public String getDmChannel() { return dmChannel; }
    public void setDmChannel(String dmChannel) { this.dmChannel = dmChannel; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isBotMessage() { return botMessage; }
    public void setBotMessage(boolean botMessage) { this.botMessage = botMessage; }
}
