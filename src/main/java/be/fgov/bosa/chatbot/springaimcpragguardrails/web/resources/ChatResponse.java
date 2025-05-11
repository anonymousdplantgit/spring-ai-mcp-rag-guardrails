package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String response;
    private UUID conversationId;
    private double confidence;
    private double retrievalConfidence;
}
