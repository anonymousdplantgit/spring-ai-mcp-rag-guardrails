package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import be.fgov.bosa.chatbot.springaimcpragguardrails.advisors.LinksAppendingAdvisor;
import be.fgov.bosa.chatbot.springaimcpragguardrails.advisors.QueryLinksAugmenter;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ChatbotRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ConversationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.MessageRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.ConversationSummary;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.mappers.ChatBotMapper;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.ChatRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ChatResponse;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ChatbotResource;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.MessageResource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@Service
@Slf4j
public class ChatBotService {

    private final ChatbotRepository repository;
    private final ChatBotMapper mapper;
    private final ChatClient ragChatClient;
    private final RagService ragService;
    private final ChatMemory ragCallingChatMemory;
    private final VectorStore vectorStore;
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    /**
     * Query the LLM with complete message persistence
     */
    @Transactional
    public ChatResponse queryLLM(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        // Validate bot exists and is active
        UUID botUuid = request.getBotId();
        Chatbot chatbot = repository.findById(botUuid)
                .orElseThrow(() -> new IllegalArgumentException("Chatbot not found with id: " + request.getBotId()));

        if (chatbot.getStatus() == ChatbotStatusEnum.ARCHIVED) {
            throw new IllegalStateException("Cannot chat with archived bot: " + chatbot.getName());
        }

        // Save user message with bot context
        MessageResource userMessage = messageService.saveUserMessage(
                request.getConversationId(),
                request.getMessage(),
                botUuid
        );

        UUID conversationId = userMessage.getConversation().getId();

        // Create a filter expression to match botId from the request
        Filter.Expression botIdFilter = new FilterExpressionBuilder().eq("botId", request.getBotId()).build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7)
                .filterExpression(botIdFilter) // Add the filter expression here
                .build();
        QueryAugmenter queryAugmenter = new QueryLinksAugmenter();
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .order(1)
                .build();
        LinksAppendingAdvisor linksAppendingAdvisor
                = LinksAppendingAdvisor.builder().order(2).build();

        try {
            // Build the chat client
            var promptBuilder = ragChatClient.prompt()
                    .advisors(
                            retrievalAugmentationAdvisor,
                            linksAppendingAdvisor,
                            new MessageChatMemoryAdvisor(ragCallingChatMemory, conversationId.toString(), 10)
                    )
                    .options(ChatOptions.builder()
                            .temperature(chatbot.getTemperature())
                            .topK(5)
                            .build());

            // Add system prompt if configured
            String systemPrompt = chatbot.getSystemPromptTemplate();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                promptBuilder = promptBuilder.system(systemPrompt);
            }

            // Get response from LLM
            var chatClientCall = promptBuilder
                    .user(request.getMessage())
                    .call();

            String responseContent = chatClientCall.content();

            // Apply response template if configured
            if (chatbot.getResponseTemplate() != null && !chatbot.getResponseTemplate().isEmpty()) {
                responseContent = applyResponseTemplate(chatbot.getResponseTemplate(), responseContent);
            }

            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;

            // Extract token usage information if available
            Integer inputTokens = null;
            Integer outputTokens = null;
            try {
                var metadata = chatClientCall.chatResponse().getMetadata();
                if (metadata != null && metadata.containsKey("usage")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> usage = (Map<String, Object>) metadata.get("usage");
                    if (usage != null) {
                        Object promptTokensObj = usage.get("promptTokens");
                        Object completionTokensObj = usage.get("completionTokens");

                        if (promptTokensObj instanceof Number) {
                            inputTokens = ((Number) promptTokensObj).intValue();
                        }
                        if (completionTokensObj instanceof Number) {
                            outputTokens = ((Number) completionTokensObj).intValue();
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Failed to extract token usage: {}", e.getMessage());
            }

            // Extract confidence score
            Double confidence = extractConfidenceScore(chatClientCall);

            // Save bot response message with all metadata and bot context
            MessageResource botMessage = messageService.saveBotMessage(
                    request.getConversationId(),
                    responseContent,
                    inputTokens,
                    outputTokens,
                    confidence,
                    processingTime,
                    botUuid
            );

            // Build and return the response
            return ChatResponse.builder()
                    .response(responseContent)
                    .conversationId(conversationId)
                    .confidence(confidence != null ? confidence : 0.0)
                    .retrievalConfidence(confidence != null ? confidence : 0.0)
                    .build();

        } catch (Exception e) {
            log.error("Error querying LLM for bot {}: {}", request.getBotId(), e.getMessage(), e);

            // Save error message
            try {
                messageService.saveBotMessage(
                        conversationId,
                        "I apologize, but I encountered an error while processing your request. Please try again.",
                        null,
                        null,
                        0.0,
                        System.currentTimeMillis() - startTime,
                        botUuid
                );
            } catch (Exception saveError) {
                log.error("Failed to save error message: {}", saveError.getMessage());
            }

            throw new RuntimeException("Failed to get response from chatbot", e);
        }
    }

    /**
     * Extract confidence score from chat client call
     */
    private Double extractConfidenceScore(ChatClient.CallResponseSpec chatClientCall) {
        try {
            var metadata = Objects.requireNonNull(chatClientCall.chatResponse()).getMetadata();
            if (metadata != null && metadata.containsKey("confidence")) {
                Object confidence = metadata.get("confidence");
                if (confidence instanceof Number) {
                    return ((Number) confidence).doubleValue();
                }
            }

            // Default confidence calculation based on response characteristics
            String content = chatClientCall.content();
            if (content != null && content.length() > 10) {
                return 0.8; // Default confidence
            }
            return 0.5;
        } catch (Exception e) {
            log.debug("Failed to extract confidence score: {}", e.getMessage());
            return 0.5;
        }
    }

    /**
     * Apply response template to format the bot's response
     */
    private String applyResponseTemplate(String template, String response) {
        // Simple template replacement - can be enhanced with more sophisticated templating
        return template.replace("{{response}}", response);
    }

    /**
     * Get conversation history for a bot
     */
    @Transactional(readOnly = true)
    public List<MessageResource> getConversationHistory(UUID conversationId) {
        return messageService.getConversationMessagesByExternalId(conversationId);
    }

    /**
     * Get conversation summary with statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getConversationSummary(UUID conversationId) {
        ConversationSummary summary = messageService.getConversationSummary(conversationId);

        return Map.of(
                "conversationId", summary.getConversationId(),
                "totalMessages", summary.getTotalMessages(),
                "userMessages", summary.getUserMessages(),
                "botMessages", summary.getBotMessages(),
                "totalTokens", summary.getTotalTokens(),
                "startTime", summary.getStartTime(),
                "lastActivity", summary.getLastActivity(),
                "duration", summary.getDuration()
        );
    }

    /**
     * Clear conversation history
     */
    @Transactional
    public void clearConversationHistory(UUID conversationId) {
        // Clear from chat memory
        ragCallingChatMemory.clear(conversationId.toString());

        // Get the conversation UUID
        UUID conversationUUID = getConversationUUID(conversationId);

        // Clear from database
        messageService.deleteConversationMessages(conversationUUID);
    }

    /**
     * Get conversation UUID from external ID
     */
    private UUID getConversationUUID(UUID conversationId) {
        return conversationRepository.findById(conversationId)
                .map(Conversation::getId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));
    }

    /**
     * Get bot message statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getBotMessageStatistics(UUID botId) {
        long totalMessages = messageService.getMessageCountForBot(botId);
        Double avgProcessingTime = messageRepository.getAverageProcessingTime(botId);
        List<MessageResource> recentMessages = messageService.getRecentMessagesForBot(botId, 10);

        // Get conversation statistics
        long totalConversations = conversationRepository.countByChatbotId(botId);
        long activeConversations = conversationRepository.countActiveConversationsByBotId(botId);

        return Map.of(
                "totalMessages", totalMessages,
                "averageProcessingTime", avgProcessingTime != null ? avgProcessingTime : 0.0,
                "recentMessages", recentMessages,
                "totalConversations", totalConversations,
                "activeConversations", activeConversations
        );
    }

    // ... [Keep all existing methods unchanged] ...

    /**
     * Get conversation count for a bot
     */
    @Transactional(readOnly = true)
    public long getConversationCountForBot(UUID botId) {
        return conversationRepository.countByChatbotId(botId);
    }

    /**
     * Get last activity for a bot
     */
    @Transactional(readOnly = true)
    public OffsetDateTime getLastActivityForBot(UUID botId) {
        return conversationRepository.getLastActivityForBot(botId).orElse(null);
    }

    /**
     * Save chatbot with validation
     */
    @Transactional
    public ChatbotResource save(ChatbotResource request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Chatbot name cannot be empty");
        }

        // Set default values if not provided
        if (request.getTemperature() == 0) {
            request.setTemperature(0.1);
        }
        if (request.getConfidenceThreshold() == 0) {
            request.setConfidenceThreshold(0.65);
        }

        return this.mapper.toResource(this.repository.save(this.mapper.toModel(request)));
    }

    /**
     * Get chatbot with additional validation
     */
    @Transactional(readOnly = true)
    public ChatbotResource getChatbot(UUID id) {
        Chatbot chatbot = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chatbot not found with id: " + id));
        return this.mapper.toResource(chatbot);
    }

    /**
     * Get all bots with status filter
     */
    @Transactional(readOnly = true)
    public List<ChatbotResource> getAllBots() {
        return mapper.toResources(repository.findAll());
    }

    /**
     * Get active bots only
     */
    @Transactional(readOnly = true)
    public List<ChatbotResource> getActiveBots() {
        List<Chatbot> activeBots = repository.findAll().stream()
                .filter(bot -> bot.getStatus() == ChatbotStatusEnum.LIVE)
                .toList();
        return mapper.toResources(activeBots);
    }

    /**
     * Find items with enhanced filtering
     */
    @Transactional(readOnly = true)
    public Page<ChatbotResource> findItems(LazyLoadingEventRequest request) {
        Sort.Direction direction = null;
        if (request.getSortField() != null) {
            direction = request.getSortOrder().equals(1) ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        Pageable pageRequest = PageRequest.of(
                request.getFirst() / request.getRows(),
                request.getRows(),
                direction != null ? Sort.by(direction, request.getSortField()) : Sort.by(Sort.Direction.ASC, "creationDate"));
        return repository.findItems(request.getGlobalFilter(), pageRequest).map(mapper::toResource);
    }

    /**
     * Ingest PDF with validation
     */
    @Transactional
    public void ingestPDF(MultipartFile file, String botId) throws IOException {
        // Validate bot exists
        repository.findById(UUID.fromString(botId))
                .orElseThrow(() -> new IllegalArgumentException("Chatbot not found with id: " + botId));

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        this.ragService.ingestPDF(file, botId);
    }

    /**
     * Clear embeddings with validation
     */
    @Transactional
    public void clearEmbeddingsByBotId(String botId) {
        // Validate bot exists
        repository.findById(UUID.fromString(botId))
                .orElseThrow(() -> new IllegalArgumentException("Chatbot not found with id: " + botId));

        this.ragService.clearEmbeddingsByBotId(botId);
    }

    /**
     * Update bot configuration
     */
    @Transactional
    public ChatbotResource updateBotConfiguration(UUID botId, Map<String, Object> updates) {
        Chatbot bot = repository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Chatbot not found with id: " + botId));

        // Apply updates
        updates.forEach((key, value) -> {
            switch (key) {
                case "name" -> bot.setName((String) value);
                case "description" -> bot.setDescription((String) value);
                case "temperature" -> bot.setTemperature(((Number) value).doubleValue());
                case "confidenceThreshold" -> bot.setConfidenceThreshold(((Number) value).doubleValue());
                case "systemPromptTemplate" -> bot.setSystemPromptTemplate((String) value);
                case "responseTemplate" -> bot.setResponseTemplate((String) value);
                case "strictGuardrails" -> bot.setStrictGuardrails((Boolean) value);
                case "customGuardrails" -> bot.setCustomGuardrails((String) value);
                default -> log.warn("Unknown update field: {}", key);
            }
        });

        return mapper.toResource(repository.save(bot));
    }
}