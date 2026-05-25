package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_channel_state", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "channel_key"})
})
public class UserChannelState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // "group_<id>" or "dm_<channel>"
    @Column(nullable = false)
    private String channelKey;

    private LocalDateTime lastReadAt;

    public UserChannelState() {}

    public UserChannelState(User user, String channelKey, LocalDateTime lastReadAt) {
        this.user = user;
        this.channelKey = channelKey;
        this.lastReadAt = lastReadAt;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getChannelKey() { return channelKey; }
    public void setChannelKey(String channelKey) { this.channelKey = channelKey; }
    public LocalDateTime getLastReadAt() { return lastReadAt; }
    public void setLastReadAt(LocalDateTime lastReadAt) { this.lastReadAt = lastReadAt; }
}

