package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Organization;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ChatbotRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.OrganizationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.BotCreationRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.BotUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class BotConfigurationService {
    private final ChatbotRepository chatbotRepository;
    private final OrganizationRepository organizationRepository;
    private final ResourceLoader resourceLoader;

    /**
     * Create a new bot with custom configuration
     */
    public Chatbot createBot(BotCreationRequest request) {
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        Chatbot bot = Chatbot.builder()
                .name(request.getName())
                .description(request.getDescription())
                .organization(organization)
                .status(ChatbotStatusEnum.DRAFT) // Start as draft
                .temperature(request.getTemperature())
                .confidenceThreshold(request.getConfidenceThreshold())
                .responseTemplate(request.getResponseTemplate())
                .strictGuardrails(request.getStrictGuardrails())
                .build();

        // Apply custom guardrails if provided
        if (request.getCustomGuardrails() != null) {
            bot.setCustomGuardrails(request.getCustomGuardrails());
        }

        // Apply custom system prompt if provided
        if (request.getSystemPrompt() != null) {
            bot.setSystemPromptTemplate(request.getSystemPrompt());
        }

        return chatbotRepository.save(bot);
    }

    /**
     * Update bot configuration
     */
    public Chatbot updateBotConfiguration(UUID botId, BotUpdateRequest request) {
        Chatbot bot = chatbotRepository.findById(botId)
                .orElseThrow(() -> new EntityNotFoundException("Bot not found"));

        // Update fields if provided
        if (request.getName() != null) bot.setName(request.getName());
        if (request.getDescription() != null) bot.setDescription(request.getDescription());
        if (request.getStatus() != null) bot.setStatus(request.getStatus());
        if (request.getTemperature() != null) bot.setTemperature(request.getTemperature());
        if (request.getConfidenceThreshold() != null) bot.setConfidenceThreshold(request.getConfidenceThreshold());
        if (request.getResponseTemplate() != null) bot.setResponseTemplate(request.getResponseTemplate());
        if (request.getCustomGuardrails() != null) bot.setCustomGuardrails(request.getCustomGuardrails());
        if (request.getSystemPrompt() != null) bot.setSystemPromptTemplate(request.getSystemPrompt());

        return chatbotRepository.save(bot);
    }
}
