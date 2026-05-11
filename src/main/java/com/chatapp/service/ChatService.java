package com.chatapp.service;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.model.Group;
import com.chatapp.model.Message;
import com.chatapp.model.User;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

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
        r.botMessage = m.isBotMessage();
        r.timestamp = m.getTimestamp();
        if (m.getGroup() != null) {
            r.type = "GROUP";
            r.groupId = m.getGroup().getId();
        } else {
            r.type = "DM";
            r.dmChannel = m.getDmChannel();
        }
        return r;
    }

    public UserDTO toUserDTO(User u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getDisplayName(), u.getAvatarColor(), u.isOnline());
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
        return messageRepo.findByGroupOrderByTimestampAsc(group)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<ChatMessageResponse> getDmHistory(String user1, String user2) {
        String channel = buildDmChannel(user1, user2);
        return messageRepo.findByDmChannelOrderByTimestampAsc(channel)
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
    public Message saveDmMessage(String senderUsername, String targetUsername, String content) {
        User sender = userRepo.findByUsername(senderUsername).orElseThrow();
        String channel = buildDmChannel(senderUsername, targetUsername);
        Message msg = new Message(content, sender, null, channel);
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

    // Extract @bot prompt from message
    public String extractBotPrompt(String content) {
        // Remove @bot tag and trim
        return content.replaceAll("(?i)@bot", "").trim();
    }

    public boolean containsBotMention(String content) {
        return content.toLowerCase().contains("@bot");
    }
}
