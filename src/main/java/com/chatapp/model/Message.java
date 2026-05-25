package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    private String dmChannel;

    private LocalDateTime timestamp;
    private LocalDateTime expiresAt;
    private boolean botMessage = false;

    // File attachment fields
    private String fileName;
    private String fileType; // MIME type
    private long fileSize; // bytes
    @Column(columnDefinition = "TEXT")
    private String fileData; // Base64 encoded file content

    // Reply support
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    private Message replyTo;

    // Edit/Delete support
    private boolean edited = false;
    private LocalDateTime editedAt;
    private boolean deleted = false;
    private LocalDateTime deletedAt;

    // Pinned
    private boolean pinned = false;
    private LocalDateTime pinnedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pinned_by_id")
    private User pinnedBy;

    public Message() {}

    public Message(String content, User sender, Group group, String dmChannel) {
        this.content = content;
        this.sender = sender;
        this.group = group;
        this.dmChannel = dmChannel;
        this.timestamp = LocalDateTime.now();
        // Use group's custom TTL if available, otherwise default 30 min
        if (group != null && group.getMessageTtlMinutes() != null) {
            if (group.getMessageTtlMinutes() > 0) {
                this.expiresAt = this.timestamp.plusMinutes(group.getMessageTtlMinutes());
            }
            // ttl=0 means never expire, expiresAt stays null
        } else {
            this.expiresAt = this.timestamp.plusMinutes(30);
        }
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
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public boolean isBotMessage() { return botMessage; }
    public void setBotMessage(boolean botMessage) { this.botMessage = botMessage; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getFileData() { return fileData; }
    public void setFileData(String fileData) { this.fileData = fileData; }

    public boolean hasFile() { return fileName != null && !fileName.isBlank(); }

    public Message getReplyTo() { return replyTo; }
    public void setReplyTo(Message replyTo) { this.replyTo = replyTo; }

    public boolean isEdited() { return edited; }
    public void setEdited(boolean edited) { this.edited = edited; }
    public LocalDateTime getEditedAt() { return editedAt; }
    public void setEditedAt(LocalDateTime editedAt) { this.editedAt = editedAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }
    public LocalDateTime getPinnedAt() { return pinnedAt; }
    public void setPinnedAt(LocalDateTime pinnedAt) { this.pinnedAt = pinnedAt; }
    public User getPinnedBy() { return pinnedBy; }
    public void setPinnedBy(User pinnedBy) { this.pinnedBy = pinnedBy; }
}
