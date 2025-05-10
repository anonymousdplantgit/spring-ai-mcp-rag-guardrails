package be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BotCreationRequest {
    @NotNull
    private UUID organizationId;

    @NotNull
    private String name;

    private String description;

    private Double temperature;

    private Double confidenceThreshold;

    private String responseTemplate;

    private Boolean strictGuardrails;

    private String customGuardrails;

    private String systemPrompt;
}

