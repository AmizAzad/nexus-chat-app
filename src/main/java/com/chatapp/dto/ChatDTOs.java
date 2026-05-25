package com.chatapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatDTOs {

    public static class ChatMessagePayload {
        public String content;
        public String type; // "GROUP" or "DM"
        public Long groupId;
        public String dmTarget;
        // File attachment
        public String fileName;
        public String fileType;
        public long fileSize;
        public String fileData;
        // Reply
        public Long replyToId;
    }

    public static class ChatMessageResponse {
        public Long id;
        public String content;
        public String senderUsername;
        public String senderDisplayName;
        public String senderAvatarColor;
        public String senderProfilePicture;
        public String type;
        public Long groupId;
        public String dmChannel;
        public LocalDateTime timestamp;
        public LocalDateTime expiresAt;
        public boolean botMessage;
        // File
        public String fileName;
        public String fileType;
        public long fileSize;
        public String fileData;
        // Reply
        public Long replyToId;
        public String replyToContent;
        public String replyToSender;
        // Edit/Delete
        public boolean edited;
        public boolean deleted;
        // Pinned
        public boolean pinned;
        // Reactions
        public Map<String, List<String>> reactions; // emoji -> list of usernames

        public ChatMessageResponse() {}
    }

    public static class UserDTO {
        public Long id;
        public String username;
        public String displayName;
        public String avatarColor;
        public boolean online;
        public String profilePicture;
        public String nickname;
        public String email;
        public String phone;
        public String linkedinUrl;
        public String address;
        public boolean profileComplete;
        public String status; // ACTIVE, AWAY, DND, INVISIBLE
        public String statusMessage;

        public UserDTO() {}
        public UserDTO(Long id, String username, String displayName, String avatarColor, boolean online) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.avatarColor = avatarColor;
            this.online = online;
        }
    }

    public static class GroupDTO {
        public Long id;
        public String name;
        public String description;
        public String iconEmoji;
        public String createdByUsername;
        public Set<String> memberUsernames;
        public List<UserDTO> members;
        public boolean privateGroup;
        public Integer messageTtlMinutes;
        public Set<String> adminUsernames;
        public long unreadCount;

        public GroupDTO() {}
    }

    public static class CreateGroupRequest {
        public String name;
        public String description;
        public String iconEmoji;
        public List<String> memberUsernames;
        public boolean privateGroup = true;
        public Integer messageTtlMinutes;
    }

    public static class AddMemberRequest {
        public String username;
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public String displayName;
        public String nickname;
        public String email;
        public String phone;
        public String linkedinUrl;
        public String address;
        public String profilePicture;
    }

    public static class LoginRequest {
        public String username;
        public String password;
    }

    public static class LoginResponse {
        public String username;
        public String displayName;
        public String avatarColor;
        public String token;
        public boolean success;
        public String message;
        public boolean profileComplete;
        public String profilePicture;
        public String status;

        public LoginResponse() {}
    }

    public static class PresenceEvent {
        public String username;
        public boolean online;
        public String displayName;
        public String avatarColor;
        public String status;

        public PresenceEvent() {}
        public PresenceEvent(String username, boolean online, String displayName, String avatarColor, String status) {
            this.username = username;
            this.online = online;
            this.displayName = displayName;
            this.avatarColor = avatarColor;
            this.status = status;
        }
    }

    public static class ProfileUpdateRequest {
        public String displayName;
        public String nickname;
        public String email;
        public String phone;
        public String linkedinUrl;
        public String address;
        public String profilePicture;
    }

    public static class TypingEvent {
        public String username;
        public String displayName;
        public String type; // GROUP or DM
        public Long groupId;
        public String dmTarget;
        public boolean typing;

        public TypingEvent() {}
    }

    public static class ReactionRequest {
        public Long messageId;
        public String emoji;
    }

    public static class ReactionEvent {
        public Long messageId;
        public String emoji;
        public String username;
        public boolean added; // true = added, false = removed
        public String type; // GROUP or DM
        public Long groupId;
        public String dmChannel;

        public ReactionEvent() {}
    }

    public static class MessageEditRequest {
        public Long messageId;
        public String content;
    }

    public static class MessageDeleteRequest {
        public Long messageId;
    }

    public static class MessageUpdateEvent {
        public Long messageId;
        public String action; // "edit", "delete", "pin", "unpin"
        public String content;
        public String type; // GROUP or DM
        public Long groupId;
        public String dmChannel;

        public MessageUpdateEvent() {}
    }

    public static class StatusUpdateRequest {
        public String status; // ACTIVE, AWAY, DND, INVISIBLE
        public String statusMessage;
    }

    public static class PasswordChangeRequest {
        public String currentPassword;
        public String newPassword;
    }

    public static class InviteRequest {
        public int maxUses;
        public int expiresInHours;
    }

    public static class InviteResponse {
        public String token;
        public String groupName;
        public Long groupId;
        public LocalDateTime expiresAt;
        public int maxUses;

        public InviteResponse() {}
    }

    public static class GroupSettingsRequest {
        public String name;
        public String description;
        public String iconEmoji;
        public boolean privateGroup;
        public Integer messageTtlMinutes;
    }

    public static class SearchResult {
        public List<ChatMessageResponse> messages;
        public long totalCount;

        public SearchResult() {}
    }

    public static class StatsResponse {
        public long totalMessages;
        public long totalUsers;
        public long totalGroups;
        public Map<String, Long> messagesByUser;
        public Map<String, Long> messagesByGroup;

        public StatsResponse() {}
    }

    public static class UnreadCountsResponse {
        public Map<String, Long> counts; // channelKey -> count

        public UnreadCountsResponse() {}
    }
}
