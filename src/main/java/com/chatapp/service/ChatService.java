package com.chatapp.service;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.model.*;
import com.chatapp.repository.*;
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
    private final ReactionRepository reactionRepo;
    private final UserChannelStateRepository channelStateRepo;
    private final GroupInviteRepository inviteRepo;
    private final ClaudeService claudeService;

    public ChatService(UserRepository userRepo, GroupRepository groupRepo,
                       MessageRepository messageRepo, ReactionRepository reactionRepo,
                       UserChannelStateRepository channelStateRepo, GroupInviteRepository inviteRepo,
                       ClaudeService claudeService) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.messageRepo = messageRepo;
        this.reactionRepo = reactionRepo;
        this.channelStateRepo = channelStateRepo;
        this.inviteRepo = inviteRepo;
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
        r.content = m.isDeleted() ? null : m.getContent();
        r.senderUsername = m.getSender().getUsername();
        r.senderDisplayName = m.getSender().getDisplayName();
        r.senderAvatarColor = m.getSender().getAvatarColor();
        r.senderProfilePicture = m.getSender().getProfilePicture();
        r.botMessage = m.isBotMessage();
        r.timestamp = m.getTimestamp();
        r.expiresAt = m.getExpiresAt();
        r.edited = m.isEdited();
        r.deleted = m.isDeleted();
        r.pinned = m.isPinned();

        if (m.getGroup() != null) {
            r.type = "GROUP";
            r.groupId = m.getGroup().getId();
        } else {
            r.type = "DM";
            r.dmChannel = m.getDmChannel();
        }

        // File attachment (only if not deleted)
        if (!m.isDeleted() && m.hasFile()) {
            r.fileName = m.getFileName();
            r.fileType = m.getFileType();
            r.fileSize = m.getFileSize();
            r.fileData = m.getFileData();
        }

        // Reply
        if (m.getReplyTo() != null) {
            r.replyToId = m.getReplyTo().getId();
            r.replyToContent = m.getReplyTo().isDeleted() ? "[deleted]" :
                (m.getReplyTo().getContent() != null ? m.getReplyTo().getContent().substring(0, Math.min(100, m.getReplyTo().getContent().length())) : "");
            r.replyToSender = m.getReplyTo().getSender().getDisplayName();
        }

        // Reactions
        List<MessageReaction> reactions = reactionRepo.findByMessage(m);
        if (!reactions.isEmpty()) {
            r.reactions = reactions.stream()
                .collect(Collectors.groupingBy(
                    MessageReaction::getEmoji,
                    Collectors.mapping(rx -> rx.getUser().getUsername(), Collectors.toList())
                ));
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
        dto.status = u.getStatus() != null ? u.getStatus().name() : "ACTIVE";
        dto.statusMessage = u.getStatusMessage();
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
        dto.privateGroup = g.isPrivateGroup();
        dto.messageTtlMinutes = g.getMessageTtlMinutes();
        Set<String> adminNames = g.getAdmins().stream().map(User::getUsername).collect(Collectors.toSet());
        adminNames.add(g.getCreatedBy().getUsername()); // creator is always admin
        dto.adminUsernames = adminNames;
        return dto;
    }

    @Transactional
    public GroupDTO createGroup(String creatorUsername, CreateGroupRequest req) {
        User creator = userRepo.findByUsername(creatorUsername).orElseThrow();
        Group group = new Group(req.name, req.description,
            req.iconEmoji != null ? req.iconEmoji : "💬", creator);
        group.getMembers().add(creator);
        group.getAdmins().add(creator);
        group.setPrivateGroup(req.privateGroup);
        group.setMessageTtlMinutes(req.messageTtlMinutes);

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
    public GroupDTO removeMember(Long groupId, String username, String requestingUser) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User requester = userRepo.findByUsername(requestingUser).orElseThrow();
        User target = userRepo.findByUsername(username).orElseThrow();

        // Can remove self (leave) or admin can remove others
        if (!username.equals(requestingUser) && !group.isAdmin(requester)) {
            throw new IllegalArgumentException("Only admins can remove members");
        }

        group.getMembers().remove(target);
        group.getAdmins().remove(target);
        group = groupRepo.save(group);
        return toGroupDTO(group);
    }

    @Transactional
    public GroupDTO updateGroupSettings(Long groupId, String username, GroupSettingsRequest req) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User user = userRepo.findByUsername(username).orElseThrow();
        if (!group.isAdmin(user)) {
            throw new IllegalArgumentException("Only admins can update group settings");
        }
        if (req.name != null && !req.name.isBlank()) group.setName(req.name);
        if (req.description != null) group.setDescription(req.description);
        if (req.iconEmoji != null) group.setIconEmoji(req.iconEmoji);
        group.setPrivateGroup(req.privateGroup);
        group.setMessageTtlMinutes(req.messageTtlMinutes);
        group = groupRepo.save(group);
        return toGroupDTO(group);
    }

    @Transactional
    public GroupDTO promoteToAdmin(Long groupId, String username, String requestingUser) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User requester = userRepo.findByUsername(requestingUser).orElseThrow();
        if (!group.isAdmin(requester)) throw new IllegalArgumentException("Only admins can promote");
        User target = userRepo.findByUsername(username).orElseThrow();
        group.getAdmins().add(target);
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
    public List<GroupDTO> getPublicGroups() {
        return groupRepo.findPublicGroups().stream().map(this::toGroupDTO).collect(Collectors.toList());
    }

    @Transactional
    public GroupDTO joinPublicGroup(Long groupId, String username) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        if (group.isPrivateGroup()) throw new IllegalArgumentException("Cannot join private group");
        User user = userRepo.findByUsername(username).orElseThrow();
        group.getMembers().add(user);
        group = groupRepo.save(group);
        return toGroupDTO(group);
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
        attachFile(msg, payload);
        attachReply(msg, payload);
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
        attachFile(msg, payload);
        attachReply(msg, payload);
        return messageRepo.save(msg);
    }

    private void attachFile(Message msg, ChatMessagePayload payload) {
        if (payload.fileName != null && !payload.fileName.isBlank()) {
            msg.setFileName(payload.fileName);
            msg.setFileType(payload.fileType);
            msg.setFileSize(payload.fileSize);
            msg.setFileData(payload.fileData);
        }
    }

    private void attachReply(Message msg, ChatMessagePayload payload) {
        if (payload.replyToId != null) {
            messageRepo.findById(payload.replyToId).ifPresent(msg::setReplyTo);
        }
    }

    @Transactional
    public Message saveBotMessage(String senderUsername, Long groupId, String dmChannel, String botReply) {
        User botUser = userRepo.findByUsername("bot").orElseGet(() -> {
            User b = new User("bot", "N/A", "Nexus Bot", "#6366f1");
            return userRepo.save(b);
        });
        Group group = groupId != null ? groupRepo.findById(groupId).orElse(null) : null;
        Message msg = new Message(botReply, botUser, group, dmChannel);
        msg.setBotMessage(true);
        return messageRepo.save(msg);
    }

    public String getBotReply(String prompt) {
        return claudeService.askBot(prompt);
    }

    public String getBotReplyWithContext(String prompt, List<Message> context) {
        return claudeService.askBotWithContext(prompt, context);
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

    // Message Edit
    @Transactional
    public Message editMessage(Long messageId, String content, String username) {
        Message msg = messageRepo.findById(messageId).orElseThrow();
        if (!msg.getSender().getUsername().equals(username)) {
            throw new IllegalArgumentException("Can only edit your own messages");
        }
        msg.setContent(content);
        msg.setEdited(true);
        msg.setEditedAt(LocalDateTime.now());
        return messageRepo.save(msg);
    }

    // Message Delete
    @Transactional
    public Message deleteMessage(Long messageId, String username) {
        Message msg = messageRepo.findById(messageId).orElseThrow();
        if (!msg.getSender().getUsername().equals(username)) {
            throw new IllegalArgumentException("Can only delete your own messages");
        }
        msg.setDeleted(true);
        msg.setDeletedAt(LocalDateTime.now());
        msg.setContent(null);
        msg.setFileData(null);
        msg.setFileName(null);
        return messageRepo.save(msg);
    }

    // Pin/Unpin
    @Transactional
    public Message togglePin(Long messageId, String username) {
        Message msg = messageRepo.findById(messageId).orElseThrow();
        User user = userRepo.findByUsername(username).orElseThrow();
        if (msg.isPinned()) {
            msg.setPinned(false);
            msg.setPinnedAt(null);
            msg.setPinnedBy(null);
        } else {
            msg.setPinned(true);
            msg.setPinnedAt(LocalDateTime.now());
            msg.setPinnedBy(user);
        }
        return messageRepo.save(msg);
    }

    @Transactional
    public List<ChatMessageResponse> getPinnedMessages(Long groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        return messageRepo.findPinnedByGroup(group).stream()
            .map(this::toResponse).collect(Collectors.toList());
    }

    // Reactions
    @Transactional
    public boolean toggleReaction(Long messageId, String username, String emoji) {
        Message msg = messageRepo.findById(messageId).orElseThrow();
        User user = userRepo.findByUsername(username).orElseThrow();
        Optional<MessageReaction> existing = reactionRepo.findByMessageAndUserAndEmoji(msg, user, emoji);
        if (existing.isPresent()) {
            reactionRepo.delete(existing.get());
            return false; // removed
        } else {
            reactionRepo.save(new MessageReaction(msg, user, emoji));
            return true; // added
        }
    }

    // Search
    @Transactional
    public SearchResult searchMessages(String query) {
        List<Message> results = messageRepo.searchMessages(query, LocalDateTime.now());
        SearchResult sr = new SearchResult();
        sr.messages = results.stream().limit(50).map(this::toResponse).collect(Collectors.toList());
        sr.totalCount = results.size();
        return sr;
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

    // Status update
    @Transactional
    public UserDTO updateStatus(String username, StatusUpdateRequest req) {
        User user = userRepo.findByUsername(username).orElseThrow();
        if (req.status != null) user.setStatus(UserStatus.valueOf(req.status));
        if (req.statusMessage != null) user.setStatusMessage(req.statusMessage);
        userRepo.save(user);
        return toUserDTO(user);
    }

    // Password change
    @Transactional
    public boolean changePassword(String username, String currentPassword, String newPassword,
                                   org.springframework.security.crypto.password.PasswordEncoder encoder) {
        User user = userRepo.findByUsername(username).orElseThrow();
        if (!encoder.matches(currentPassword, user.getPassword())) return false;
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        return true;
    }

    // Account deletion
    @Transactional
    public void deleteAccount(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        // Remove from all groups
        for (Group g : new ArrayList<>(user.getGroups())) {
            g.getMembers().remove(user);
            g.getAdmins().remove(user);
            groupRepo.save(g);
        }
        userRepo.delete(user);
    }

    // Invite links
    @Transactional
    public InviteResponse createInvite(Long groupId, String username, InviteRequest req) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        User user = userRepo.findByUsername(username).orElseThrow();
        if (!group.isAdmin(user)) throw new IllegalArgumentException("Only admins can create invites");
        GroupInvite invite = new GroupInvite(group, user, req.maxUses, req.expiresInHours);
        invite = inviteRepo.save(invite);
        InviteResponse res = new InviteResponse();
        res.token = invite.getToken();
        res.groupName = group.getName();
        res.groupId = group.getId();
        res.expiresAt = invite.getExpiresAt();
        res.maxUses = invite.getMaxUses();
        return res;
    }

    @Transactional
    public GroupDTO acceptInvite(String token, String username) {
        GroupInvite invite = inviteRepo.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid invite"));
        if (!invite.isValid()) throw new IllegalArgumentException("Invite expired or used up");
        User user = userRepo.findByUsername(username).orElseThrow();
        Group group = invite.getGroup();
        group.getMembers().add(user);
        invite.setUsedCount(invite.getUsedCount() + 1);
        inviteRepo.save(invite);
        group = groupRepo.save(group);
        return toGroupDTO(group);
    }

    // Unread counts
    @Transactional
    public void markRead(String username, String channelKey) {
        User user = userRepo.findByUsername(username).orElseThrow();
        UserChannelState state = channelStateRepo.findByUserAndChannelKey(user, channelKey)
            .orElse(new UserChannelState(user, channelKey, LocalDateTime.now()));
        state.setLastReadAt(LocalDateTime.now());
        channelStateRepo.save(state);
    }

    @Transactional
    public Map<String, Long> getUnreadCounts(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        Map<String, Long> counts = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Groups
        for (Group g : groupRepo.findByMember(user)) {
            String key = "group_" + g.getId();
            UserChannelState state = channelStateRepo.findByUserAndChannelKey(user, key).orElse(null);
            LocalDateTime since = state != null ? state.getLastReadAt() : LocalDateTime.MIN;
            long count = messageRepo.countUnreadInGroup(g, since, now);
            if (count > 0) counts.put(key, count);
        }

        return counts;
    }

    // AI Summarize
    @Transactional
    public String summarizeGroup(Long groupId) {
        Group group = groupRepo.findById(groupId).orElseThrow();
        List<Message> recent = messageRepo.findRecentByGroup(group, LocalDateTime.now());
        if (recent.isEmpty()) return "No messages to summarize.";
        List<Message> last50 = recent.subList(0, Math.min(50, recent.size()));
        Collections.reverse(last50); // chronological order
        return claudeService.askBotWithContext("Summarize the following chat conversation concisely:", last50);
    }

    // Stats
    @Transactional
    public StatsResponse getStats() {
        StatsResponse stats = new StatsResponse();
        stats.totalUsers = userRepo.count();
        stats.totalGroups = groupRepo.count();
        stats.totalMessages = messageRepo.count();
        // Per-user counts - simplified
        stats.messagesByUser = new HashMap<>();
        stats.messagesByGroup = new HashMap<>();
        return stats;
    }

    // Extract @mentions from content
    public Set<String> extractMentions(String content) {
        Set<String> mentions = new HashSet<>();
        if (content == null) return mentions;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("@([a-zA-Z0-9_]+)").matcher(content);
        while (m.find()) {
            String mentioned = m.group(1);
            if (!"bot".equalsIgnoreCase(mentioned)) {
                mentions.add(mentioned);
            }
        }
        return mentions;
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
