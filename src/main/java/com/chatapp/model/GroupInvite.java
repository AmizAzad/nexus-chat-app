package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_invites")
public class GroupInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private int maxUses = 0; // 0 = unlimited
    private int usedCount = 0;

    public GroupInvite() {}

    public GroupInvite(Group group, User createdBy, int maxUses, int expiresInHours) {
        this.group = group;
        this.createdBy = createdBy;
        this.token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        this.createdAt = LocalDateTime.now();
        this.maxUses = maxUses;
        if (expiresInHours > 0) {
            this.expiresAt = this.createdAt.plusHours(expiresInHours);
        }
    }

    public boolean isValid() {
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) return false;
        if (maxUses > 0 && usedCount >= maxUses) return false;
        return true;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public Group getGroup() { return group; }
    public User getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public int getMaxUses() { return maxUses; }
    public int getUsedCount() { return usedCount; }
    public void setUsedCount(int usedCount) { this.usedCount = usedCount; }
}

