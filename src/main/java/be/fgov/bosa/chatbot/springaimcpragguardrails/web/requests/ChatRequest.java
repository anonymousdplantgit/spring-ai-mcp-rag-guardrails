package be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests;

import lombok.*;

import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {
    private String message;
    private UUID conversationId;
    private UUID botId;
}