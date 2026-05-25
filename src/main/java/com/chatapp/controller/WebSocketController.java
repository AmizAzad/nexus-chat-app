package com.chatapp.controller;

import com.chatapp.config.RateLimiter;
import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.model.Message;
import com.chatapp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final RateLimiter rateLimiter;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService, RateLimiter rateLimiter) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.rateLimiter = rateLimiter;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessagePayload payload, Principal principal) {
        String senderUsername = principal.getName();

        // Rate limiting
        if (!rateLimiter.isAllowed(senderUsername)) {
            ChatMessageResponse error = new ChatMessageResponse();
            error.content = "⚠️ Slow down! You're sending messages too fast.";
            error.senderUsername = "system";
            error.senderDisplayName = "System";
            error.senderAvatarColor = "#ef4444";
            error.botMessage = true;
            error.type = payload.type;
            if ("GROUP".equals(payload.type)) {
                messagingTemplate.convertAndSend("/topic/group/" + payload.groupId, error);
            } else {
                messagingTemplate.convertAndSendToUser(senderUsername, "/queue/dm", error);
            }
            return;
        }

        // Validate file size if file is attached
        if (payload.fileName != null && !payload.fileName.isBlank()) {
            if (!chatService.isValidFileSize(payload.fileSize)) {
                ChatMessageResponse error = new ChatMessageResponse();
                error.content = "⚠️ File size must be less than 2 MB.";
                error.senderUsername = "system";
                error.senderDisplayName = "System";
                error.senderAvatarColor = "#ef4444";
                error.botMessage = true;
                error.type = payload.type;
                if ("GROUP".equals(payload.type)) {
                    messagingTemplate.convertAndSend("/topic/group/" + payload.groupId, error);
                } else {
                    messagingTemplate.convertAndSendToUser(senderUsername, "/queue/dm", error);
                }
                return;
            }
        }

        if ("GROUP".equals(payload.type)) {
            handleGroupMessage(senderUsername, payload);
        } else if ("DM".equals(payload.type)) {
            handleDmMessage(senderUsername, payload);
        }
    }

    private void handleGroupMessage(String senderUsername, ChatMessagePayload payload) {
        Message saved;
        if ((payload.fileName != null && !payload.fileName.isBlank()) || payload.replyToId != null) {
            saved = chatService.saveGroupMessageWithFile(senderUsername, payload.groupId, payload);
        } else {
            saved = chatService.saveGroupMessage(senderUsername, payload.groupId, payload.content);
        }
        ChatMessageResponse response = chatService.toResponse(saved);
        messagingTemplate.convertAndSend("/topic/group/" + payload.groupId, response);

        // Handle @mentions - notify mentioned users
        Set<String> mentions = chatService.extractMentions(payload.content);
        for (String mentioned : mentions) {
            messagingTemplate.convertAndSendToUser(mentioned, "/queue/notifications",
                new MentionNotification(senderUsername, response.senderDisplayName, payload.content, "GROUP", payload.groupId, null));
        }

        // Bot mention handling
        if (chatService.containsBotMention(payload.content)) {
            String prompt = chatService.extractBotPrompt(payload.content);
            new Thread(() -> {
                String botReply = chatService.getBotReply(prompt);
                Message botMsg = chatService.saveBotMessage(senderUsername, payload.groupId, null, botReply);
                ChatMessageResponse botResponse = chatService.toResponse(botMsg);
                messagingTemplate.convertAndSend("/topic/group/" + payload.groupId, botResponse);
            }).start();
        }
    }

    private void handleDmMessage(String senderUsername, ChatMessagePayload payload) {
        String targetUsername = payload.dmTarget;

        Message saved;
        if ((payload.fileName != null && !payload.fileName.isBlank()) || payload.replyToId != null) {
            saved = chatService.saveDmMessageWithFile(senderUsername, targetUsername, payload);
        } else {
            saved = chatService.saveDmMessage(senderUsername, targetUsername, payload.content);
        }
        ChatMessageResponse response = chatService.toResponse(saved);

        messagingTemplate.convertAndSendToUser(senderUsername, "/queue/dm", response);
        messagingTemplate.convertAndSendToUser(targetUsername, "/queue/dm", response);

        // Handle @mentions in DMs
        Set<String> mentions = chatService.extractMentions(payload.content);
        for (String mentioned : mentions) {
            if (!mentioned.equals(targetUsername)) {
                messagingTemplate.convertAndSendToUser(mentioned, "/queue/notifications",
                    new MentionNotification(senderUsername, response.senderDisplayName, payload.content, "DM", null, response.dmChannel));
            }
        }

        if (chatService.containsBotMention(payload.content)) {
            String prompt = chatService.extractBotPrompt(payload.content);
            String dmChannel = chatService.buildDmChannel(senderUsername, targetUsername);
            new Thread(() -> {
                String botReply = chatService.getBotReply(prompt);
                Message botMsg = chatService.saveBotMessage(senderUsername, null, dmChannel, botReply);
                ChatMessageResponse botResponse = chatService.toResponse(botMsg);
                messagingTemplate.convertAndSendToUser(senderUsername, "/queue/dm", botResponse);
                messagingTemplate.convertAndSendToUser(targetUsername, "/queue/dm", botResponse);
            }).start();
        }
    }

    @MessageMapping("/presence")
    public void presence(@Payload PresenceEvent event, Principal principal) {
        event.username = principal.getName();
        messagingTemplate.convertAndSend("/topic/presence", event);
    }

    @MessageMapping("/typing")
    public void typing(@Payload TypingEvent event, Principal principal) {
        event.username = principal.getName();
        if ("GROUP".equals(event.type)) {
            messagingTemplate.convertAndSend("/topic/typing/group/" + event.groupId, event);
        } else if ("DM".equals(event.type)) {
            messagingTemplate.convertAndSendToUser(event.dmTarget, "/queue/typing", event);
        }
    }

    @MessageMapping("/reaction")
    public void reaction(@Payload ReactionRequest req, Principal principal) {
        String username = principal.getName();
        boolean added = chatService.toggleReaction(req.messageId, username, req.emoji);
        ReactionEvent event = new ReactionEvent();
        event.messageId = req.messageId;
        event.emoji = req.emoji;
        event.username = username;
        event.added = added;
        messagingTemplate.convertAndSend("/topic/reactions", event);
    }

    @MessageMapping("/message.edit")
    public void editMessage(@Payload MessageEditRequest req, Principal principal) {
        String username = principal.getName();
        try {
            Message edited = chatService.editMessage(req.messageId, req.content, username);
            MessageUpdateEvent event = new MessageUpdateEvent();
            event.messageId = req.messageId;
            event.action = "edit";
            event.content = req.content;
            if (edited.getGroup() != null) {
                event.type = "GROUP";
                event.groupId = edited.getGroup().getId();
                messagingTemplate.convertAndSend("/topic/group/" + event.groupId + "/updates", event);
            } else {
                event.type = "DM";
                event.dmChannel = edited.getDmChannel();
                messagingTemplate.convertAndSend("/topic/dm/updates", event);
            }
        } catch (Exception e) {
            // silently fail
        }
    }

    @MessageMapping("/message.delete")
    public void deleteMessage(@Payload MessageDeleteRequest req, Principal principal) {
        String username = principal.getName();
        try {
            Message deleted = chatService.deleteMessage(req.messageId, username);
            MessageUpdateEvent event = new MessageUpdateEvent();
            event.messageId = req.messageId;
            event.action = "delete";
            if (deleted.getGroup() != null) {
                event.type = "GROUP";
                event.groupId = deleted.getGroup().getId();
                messagingTemplate.convertAndSend("/topic/group/" + event.groupId + "/updates", event);
            } else {
                event.type = "DM";
                event.dmChannel = deleted.getDmChannel();
                messagingTemplate.convertAndSend("/topic/dm/updates", event);
            }
        } catch (Exception e) {
            // silently fail
        }
    }

    @MessageMapping("/message.pin")
    public void pinMessage(@Payload MessageDeleteRequest req, Principal principal) {
        String username = principal.getName();
        try {
            Message pinned = chatService.togglePin(req.messageId, username);
            MessageUpdateEvent event = new MessageUpdateEvent();
            event.messageId = req.messageId;
            event.action = pinned.isPinned() ? "pin" : "unpin";
            if (pinned.getGroup() != null) {
                event.type = "GROUP";
                event.groupId = pinned.getGroup().getId();
                messagingTemplate.convertAndSend("/topic/group/" + event.groupId + "/updates", event);
            }
        } catch (Exception e) {
            // silently fail
        }
    }

    // Simple inner class for mention notifications
    public static class MentionNotification {
        public String fromUsername;
        public String fromDisplayName;
        public String content;
        public String type;
        public Long groupId;
        public String dmChannel;

        public MentionNotification() {}
        public MentionNotification(String fromUsername, String fromDisplayName, String content, String type, Long groupId, String dmChannel) {
            this.fromUsername = fromUsername;
            this.fromDisplayName = fromDisplayName;
            this.content = content;
            this.type = type;
            this.groupId = groupId;
            this.dmChannel = dmChannel;
        }
    }
}
