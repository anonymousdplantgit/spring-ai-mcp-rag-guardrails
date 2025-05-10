package be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ChatRequest {
    private String message;
    private String conversationId;
    private String botId;
}