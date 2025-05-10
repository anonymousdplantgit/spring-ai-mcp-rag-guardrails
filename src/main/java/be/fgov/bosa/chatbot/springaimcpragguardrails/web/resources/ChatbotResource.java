package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import lombok.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ChatbotResource {
    private UUID id;
    private String name;
    private String description;
    private ChatbotStatusEnum status;
    private OrganizationResource organization;
    private String systemPromptTemplate;
    private double temperature;
    private double confidenceThreshold ;
    private String responseTemplate;
    private Map<String, String> metadataFilters = new HashMap<>();
    private boolean strictGuardrails;
    private String customGuardrails;
    
}

