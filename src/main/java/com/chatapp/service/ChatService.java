package com.chatapp.service;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.model.Group;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final MessageRepository messageRepo;
    private final ClaudeService claudeService;

    public ChatService(UserRepository userRepo, GroupRepository groupRepo,
                       MessageRepository messageRepo, ClaudeService claudeService) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.messageRepo = messageRepo;
        this.claudeService = claudeService;
    }

    public String buildDmChannel(String u1, String u2) {
        List<String> names = Arrays.asList(u1, u2);
        Collections.sort(names);
        return names.get(0) + "__" + names.get(1);
    }

    public ChatMessageResponse toResponse(Message m) {
        ChatMessageResponse r = new ChatMessageResponse();
        r.id = m.getId();
        r.content = m.getContent();
        r.senderUsername = m.getSender().getUsername();
        r.senderDisplayName = m.getSender().getDisplayName();
        r.senderAvatarColor = m.getSender().getAvatarColor();
        r.senderProfilePicture = m.getSender().getProfilePicture();
        r.botMessage = m.isBotMessage();
        r.timestamp = m.getTimestamp();
        r.expiresAt = m.getExpiresAt();
        if (m.getGroup() != null) {
            r.type = "GROUP";
            r.groupId = m.getGroup().getId();
        } else {
            r.type = "DM";
            r.dmChannel = m.getDmChannel();
        }
        // File attachment
        if (m.hasFile()) {
            r.fileName = m.getFileName();
            r.fileType = m.getFileType();
            r.fileSize = m.getFileSize();
            r.fileData = m.getFileData();
        }
        return r;
    }

    public UserDTO toUserDTO(User u) {
        UserDTO dto = new UserDTO(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarColor(), u.isOnline());
        dto.profilePicture = u.getProfilePicture();
        dto.nickname = u.getNickname();
        dto.email = u.getEmail();
        dto.phone = u.getPhone();
        dto.linkedinUrl = u.getLinkedinUrl();
        dto.address = u.getAddress();
        dto.profileComplete = u.isProfileComplete();
        return dto;
    }

    public GroupDTO toGroupDTO(Group g) {
        GroupDTO dto = new GroupDTO();
        dto.id = g.getId();
        dto.name = g.getName();
        dto.description = g.getDescription();
        dto.iconEmoji = g.getIconEmoji();
        dto.createdByUsername = g.getCreatedBy().getUsername();
        dto.memberUsernames = g.getMembers().stream().map(User::getUsername).collect(Collectors.toSet());
        dto.members = g.getMembers().stream().map(this::toUserDTO).collect(Collectors.toList());
        return dto;
    }

    @Transactional
    public GroupDTO createGroup(String creatorUsername, CreateGroupRequest req) {
        User creator = userRepo.findByUsername(creatorUsername).orElseThrow();
        Group group = new Group(req.name, req.description,
            req.iconEmoji != null ? req.iconEmoji : "💬", creator);
        group.getMembers().add(creator);

        if (req.memberUsernames != null) {
            for (String uname : req.memberUsernames) {
                userRepo.findByUsername(uname).ifPresent(group.getMembers()::add);
            }
        }
        group = groupRepo.save(group);
        return toGroupDTO(group);
    }

    @Transactional
    public GroupDTO addMember(Long groupId, String username) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User user = userRepo.findByUsername(username).orElseThrow();
        group.getMembers().add(user);
        group = groupRepo.save(group);
        return toGroupDTO(group);
    }

    @Transactional
    public List<ChatMessageResponse> getGroupHistory(Long groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        return messageRepo.findActiveByGroup(group, LocalDateTime.now())
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<ChatMessageResponse> getDmHistory(String user1, String user2) {
        String channel = buildDmChannel(user1, user2);
        return messageRepo.findActiveByDmChannel(channel, LocalDateTime.now())
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<GroupDTO> getUserGroups(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return groupRepo.findByMember(user).stream().map(this::toGroupDTO).collect(Collectors.toList());
    }

    @Transactional
    public List<UserDTO> getAllUsers(String excludeUsername) {
        return userRepo.findAll().stream()
            .filter(u -> !u.getUsername().equals(excludeUsername))
            .map(this::toUserDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public Message saveGroupMessage(String senderUsername, Long groupId, String content) {
        User sender = userRepo.findByUsername(senderUsername).orElseThrow();
        Group group = groupRepo.findById(groupId).orElseThrow();
        Message msg = new Message(content, sender, group, null);
        return messageRepo.save(msg);
    }

    @Transactional
    public Message saveGroupMessageWithFile(String senderUsername, Long groupId, ChatMessagePayload payload) {
        User sender = userRepo.findByUsername(senderUsername).orElseThrow();
        Group group = groupRepo.findById(groupId).orElseThrow();
        Message msg = new Message(payload.content != null ? payload.content : "", sender, group, null);
        if (payload.fileName != null && !payload.fileName.isBlank()) {
            msg.setFileName(payload.fileName);
            msg.setFileType(payload.fileType);
            msg.setFileSize(payload.fileSize);
            msg.setFileData(payload.fileData);
        }
        return messageRepo.save(msg);
    }

    @Transactional
    public Message saveDmMessage(String senderUsername, String targetUsername, String content) {
        User sender = userRepo.findByUsername(senderUsername).orElseThrow();
        String channel = buildDmChannel(senderUsername, targetUsername);
        Message msg = new Message(content, sender, null, channel);
        return messageRepo.save(msg);
    }

    @Transactional
    public Message saveDmMessageWithFile(String senderUsername, String targetUsername, ChatMessagePayload payload) {
        User sender = userRepo.findByUsername(senderUsername).orElseThrow();
        String channel = buildDmChannel(senderUsername, targetUsername);
        Message msg = new Message(payload.content != null ? payload.content : "", sender, null, channel);
        if (payload.fileName != null && !payload.fileName.isBlank()) {
            msg.setFileName(payload.fileName);
            msg.setFileType(payload.fileType);
            msg.setFileSize(payload.fileSize);
            msg.setFileData(payload.fileData);
        }
        return messageRepo.save(msg);
    }

    @Transactional
    public Message saveBotMessage(String senderUsername, Long groupId, String dmChannel, String botReply) {
        User botUser = userRepo.findByUsername("bot").orElseGet(() -> {
            User b = new User("bot", "N/A", "Nexus Bot", "#6366f1");
            return userRepo.save(b);
        });
        Message msg = new Message(botReply, botUser, 
            groupId != null ? groupRepo.findById(groupId).orElse(null) : null, 
            dmChannel);
        msg.setBotMessage(true);
        return messageRepo.save(msg);
    }

    public String getBotReply(String prompt) {
        return claudeService.askBot(prompt);
    }

    public String extractBotPrompt(String content) {
        return content.replaceAll("(?i)@bot", "").trim();
    }

    public boolean containsBotMention(String content) {
        return content != null && content.toLowerCase().contains("@bot");
    }

    public boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }

    // Profile management
    @Transactional
    public UserDTO updateProfile(String username, ProfileUpdateRequest req) {
        User user = userRepo.findByUsername(username).orElseThrow();
        if (req.displayName != null && !req.displayName.isBlank()) user.setDisplayName(req.displayName);
        if (req.nickname != null) user.setNickname(req.nickname);
        if (req.email != null) user.setEmail(req.email);
        if (req.phone != null) user.setPhone(req.phone);
        if (req.linkedinUrl != null) user.setLinkedinUrl(req.linkedinUrl);
        if (req.address != null) user.setAddress(req.address);
        if (req.profilePicture != null) user.setProfilePicture(req.profilePicture);
        user.setProfileComplete(true);
        userRepo.save(user);
        return toUserDTO(user);
    }

    @Transactional
    public UserDTO getProfile(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return toUserDTO(user);
    }

    // TTL cleanup - runs every minute
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredMessages() {
        int deleted = messageRepo.deleteExpiredMessages(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired messages", deleted);
        }
    }
}
