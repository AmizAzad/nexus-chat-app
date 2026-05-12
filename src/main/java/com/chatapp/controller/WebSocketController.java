package com.chatapp.controller;

import com.chatapp.dto.ChatDTOs.*;
import com.chatapp.model.Message;
import com.chatapp.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    public WebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessagePayload payload, Principal principal) {
        String senderUsername = principal.getName();

        // Validate file size if file is attached
        if (payload.fileName != null && !payload.fileName.isBlank()) {
            if (!chatService.isValidFileSize(payload.fileSize)) {
                // Send error back to sender
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
        if (payload.fileName != null && !payload.fileName.isBlank()) {
            saved = chatService.saveGroupMessageWithFile(senderUsername, payload.groupId, payload);
        } else {
            saved = chatService.saveGroupMessage(senderUsername, payload.groupId, payload.content);
        }
        ChatMessageResponse response = chatService.toResponse(saved);
        messagingTemplate.convertAndSend("/topic/group/" + payload.groupId, response);

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
        if (payload.fileName != null && !payload.fileName.isBlank()) {
            saved = chatService.saveDmMessageWithFile(senderUsername, targetUsername, payload);
        } else {
            saved = chatService.saveDmMessage(senderUsername, targetUsername, payload.content);
        }
        ChatMessageResponse response = chatService.toResponse(saved);

        messagingTemplate.convertAndSendToUser(senderUsername, "/queue/dm", response);
        messagingTemplate.convertAndSendToUser(targetUsername, "/queue/dm", response);

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
}
