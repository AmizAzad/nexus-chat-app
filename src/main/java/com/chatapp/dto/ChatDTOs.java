package com.chatapp.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class ChatDTOs {

    public static class ChatMessagePayload {
        public String content;
        public String type; // "GROUP" or "DM"
        public Long groupId;
        public String dmTarget; // username of DM recipient
    }

    public static class ChatMessageResponse {
        public Long id;
        public String content;
        public String senderUsername;
        public String senderDisplayName;
        public String senderAvatarColor;
        public String type;
        public Long groupId;
        public String dmChannel;
        public LocalDateTime timestamp;
        public boolean botMessage;

        public ChatMessageResponse() {}
    }

    public static class UserDTO {
        public Long id;
        public String username;
        public String displayName;
        public String avatarColor;
        public boolean online;

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

        public GroupDTO() {}
    }

    public static class CreateGroupRequest {
        public String name;
        public String description;
        public String iconEmoji;
        public List<String> memberUsernames;
    }

    public static class AddMemberRequest {
        public String username;
    }

    public static class RegisterRequest {
        public String username;
        public String password;
        public String displayName;
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

        public LoginResponse() {}
    }

    public static class PresenceEvent {
        public String username;
        public boolean online;
        public String displayName;
        public String avatarColor;

        public PresenceEvent() {}
        public PresenceEvent(String username, boolean online, String displayName, String avatarColor) {
            this.username = username;
            this.online = online;
            this.displayName = displayName;
            this.avatarColor = avatarColor;
        }
    }
}
