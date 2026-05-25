package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "chat_groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    private String iconEmoji = "💬";

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt;

    // Privacy
    private boolean privateGroup = true;

    // Configurable TTL in minutes (null = default 30, 0 = never expire)
    private Integer messageTtlMinutes;

    @ManyToMany
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // Admin members
    @ManyToMany
    @JoinTable(
        name = "group_admins",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();

    public Group() {}

    public Group(String name, String description, String iconEmoji, User createdBy) {
        this.name = name;
        this.description = description;
        this.iconEmoji = iconEmoji;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }
    public User getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }

    public boolean isPrivateGroup() { return privateGroup; }
    public void setPrivateGroup(boolean privateGroup) { this.privateGroup = privateGroup; }

    public Integer getMessageTtlMinutes() { return messageTtlMinutes; }
    public void setMessageTtlMinutes(Integer messageTtlMinutes) { this.messageTtlMinutes = messageTtlMinutes; }

    public Set<User> getAdmins() { return admins; }
    public void setAdmins(Set<User> admins) { this.admins = admins; }

    public boolean isAdmin(User user) {
        return createdBy.getId().equals(user.getId()) || admins.stream().anyMatch(a -> a.getId().equals(user.getId()));
    }
}
