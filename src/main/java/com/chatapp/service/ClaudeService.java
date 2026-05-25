package com.chatapp.service;

import com.chatapp.model.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ClaudeService {

    @Value("${claude.api.key:}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ClaudeService() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public String askBot(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "⚠️ Bot is not configured. Please set `CLAUDE_API_KEY` environment variable and restart the app.";
        }

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", "claude-haiku-4-5-20251001");
            body.put("max_tokens", 1024);

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            body.set("messages", messages);

            String response = webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            return root.path("content").get(0).path("text").asText("I couldn't generate a response.");

        } catch (Exception e) {
            return "⚠️ Bot error: " + e.getMessage();
        }
    }

    public String askBotWithContext(String systemPrompt, List<Message> contextMessages) {
        if (apiKey == null || apiKey.isBlank()) {
            return "⚠️ Bot is not configured. Please set `CLAUDE_API_KEY` environment variable and restart the app.";
        }

        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", "claude-haiku-4-5-20251001");
            body.put("max_tokens", 1024);
            body.put("system", "You are Nexus Bot, an AI assistant in a chat application. Be helpful, concise, and friendly.");

            // Build conversation context
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append(systemPrompt).append("\n\n");
            for (Message msg : contextMessages) {
                if (msg.getContent() != null && !msg.getContent().isBlank()) {
                    contextBuilder.append(msg.getSender().getDisplayName())
                        .append(": ")
                        .append(msg.getContent())
                        .append("\n");
                }
            }

            ArrayNode messages = objectMapper.createArrayNode();
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", contextBuilder.toString());
            messages.add(userMsg);
            body.set("messages", messages);

            String response = webClient.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body.toString())
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(response);
            return root.path("content").get(0).path("text").asText("I couldn't generate a response.");

        } catch (Exception e) {
            return "⚠️ Bot error: " + e.getMessage();
        }
    }
}
