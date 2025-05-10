package be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests;

import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotUpdateRequest {
    private String name;

    private String description;

    private ChatbotStatusEnum status;

    private Double temperature;

    private Double confidenceThreshold;

    private String responseTemplate;

    private Boolean strictGuardrails;

    private String customGuardrails;

    private String systemPrompt;
}
