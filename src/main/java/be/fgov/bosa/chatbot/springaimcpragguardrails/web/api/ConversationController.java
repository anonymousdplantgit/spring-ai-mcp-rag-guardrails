package be.fgov.bosa.chatbot.springaimcpragguardrails.web.api;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Message;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ConversationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.MessageRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.ConversationStatistics;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.ConversationSummary;
import be.fgov.bosa.chatbot.springaimcpragguardrails.mappers.ConversationMapper;
import be.fgov.bosa.chatbot.springaimcpragguardrails.mappers.MessageMapper;
import be.fgov.bosa.chatbot.springaimcpragguardrails.services.ConversationService;
import be.fgov.bosa.chatbot.springaimcpragguardrails.services.MessageService;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ConversationResource;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversations", description = "Manage conversations and messages in the system")
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @GetMapping(value = "/search")
    @Operation(summary = "Search conversations", description = "Search conversations with autocomplete functionality")
    public ResponseEntity<List<ConversationResource>> searchConversations(
            @RequestParam @Parameter(description = "Search query for conversation autocomplete") String query) {
        return ResponseEntity.ok(conversationService.autocomplete(query));
    }

    @GetMapping
    @Operation(summary = "Get conversations", description = "Retrieve conversations with pagination and filtering")
    public ResponseEntity<Page<ConversationResource>> getConversations(
            @ModelAttribute @Parameter(description = "Lazy loading request parameters") LazyLoadingEventRequest request) {
        return ResponseEntity.ok(conversationService.findItems(request));
    }

    @PostMapping
    @Operation(summary = "Save conversation", description = "Create or update a conversation")
    public ResponseEntity<ConversationResource> saveConversation(
            @RequestBody @Parameter(description = "Conversation details") ConversationResource item) {
        return ResponseEntity.ok(conversationService.saveConversation(item));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get conversation", description = "Retrieve a specific conversation by ID")
    public ResponseEntity<ConversationResource> getConversation(
            @PathVariable @Parameter(description = "UUID of the conversation") UUID id) {
        return ResponseEntity.ok(conversationService.findConversation(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete conversation", description = "Delete a conversation and all its messages")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable @Parameter(description = "UUID of the conversation") UUID id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    // Message-related endpoints

    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "Get conversation messages", description = "Retrieve all messages for a specific conversation")
    public ResponseEntity<List<MessageResource>> getConversationMessages(
            @PathVariable @Parameter(description = "UUID of the conversation") UUID conversationId) {
        try {
            List<MessageResource> messages = messageService.getConversationMessages(conversationId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error retrieving messages for conversation {}: {}", conversationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/external/{conversationId}/messages")
    @Operation(summary = "Get messages by external ID", description = "Retrieve messages using external conversation ID")
    public ResponseEntity<List<MessageResource>> getMessagesByExternalId(
            @PathVariable @Parameter(description = "External conversation ID") UUID conversationId) {
        try {
            List<MessageResource> messages = messageService.getConversationMessagesByExternalId(conversationId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error retrieving messages for external conversation ID {}: {}", conversationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{conversationId}/messages/paged")
    @Operation(summary = "Get paginated messages", description = "Retrieve messages with pagination")
    public ResponseEntity<Page<MessageResource>> getConversationMessagesPage(
            @PathVariable @Parameter(description = "UUID of the conversation") UUID conversationId,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "50") @Parameter(description = "Page size") int size) {
        try {
            Page<MessageResource> messages = messageService.getConversationMessagesPage(
                    conversationId, PageRequest.of(page, size));
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Error retrieving paginated messages for conversation {}: {}", conversationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/external/{conversationId}/summary")
    @Operation(summary = "Get conversation summary", description = "Get conversation statistics and summary")
    public ResponseEntity<ConversationSummary> getConversationSummary(
            @PathVariable @Parameter(description = "conversation ID") UUID conversationId) {
        try {
            ConversationSummary summary = messageService.getConversationSummary(conversationId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error retrieving summary for conversation {}: {}", conversationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{conversationId}/messages")
    @Operation(summary = "Delete conversation messages", description = "Delete all messages for a conversation")
    public ResponseEntity<Void> deleteConversationMessages(
            @PathVariable @Parameter(description = "UUID of the conversation") UUID conversationId) {
        try {
            messageService.deleteConversationMessages(conversationId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting messages for conversation {}: {}", conversationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Advanced search endpoints

    @GetMapping("/search/messages")
    @Operation(summary = "Search messages", description = "Search for messages containing specific text")
    public ResponseEntity<List<MessageResource>> searchMessages(
            @RequestParam @Parameter(description = "Search text") String query,
            @RequestParam(required = false) @Parameter(description = "Conversation ID to search within") UUID conversationId,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "50") @Parameter(description = "Page size") int size) {
        try {
            List<Message> messages;
            if (conversationId != null) {
                messages = messageRepository.searchMessagesInConversation(conversationId, query);
            } else {
                // Global search across all conversations
                messages = messageRepository.searchMessagesGlobal(query, PageRequest.of(page, size)).getContent();
            }
            return ResponseEntity.ok(messageMapper.toResources(messages));
        } catch (Exception e) {
            log.error("Error searching messages: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/conversations")
    @Operation(summary = "Get bot conversations", description = "Get all conversations for a specific bot")
    public ResponseEntity<Page<ConversationResource>> getBotConversations(
            @PathVariable @Parameter(description = "Bot ID") UUID botId,
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size) {
        try {
            Page<Conversation> conversations = conversationRepository.findByChatbotId(
                    botId, PageRequest.of(page, size));
            return ResponseEntity.ok(conversations.map(conversationMapper::toResource));
        } catch (Exception e) {
            log.error("Error retrieving conversations for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bot/{botId}/stats")
    @Operation(summary = "Get bot conversation stats", description = "Get conversation statistics for a bot")
    public ResponseEntity<Map<String, Object>> getBotConversationStats(
            @PathVariable @Parameter(description = "Bot ID") UUID botId) {
        try {
            ConversationStatistics stats = conversationRepository.getConversationStatistics(botId);
            Long totalConversations = stats.getTotalConversations();
            Double avgMessages = stats.getAvgMessagesPerConversation();
            Long activeConversations = stats.getActiveConversations();
            OffsetDateTime lastActivity = conversationRepository.getLastActivityForBot(botId).orElse(null);

            Map<String, Object> response = Map.of(
                    "totalConversations", totalConversations != null ? totalConversations : 0,
                    "averageMessagesPerConversation", avgMessages != null ? avgMessages : 0.0,
                    "activeConversations", activeConversations != null ? activeConversations : 0,
                    "lastActivity", lastActivity != null ? lastActivity.toString() : null,
                    "totalMessages", messageService.getMessageCountForBot(botId)
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving stats for bot {}: {}", botId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/range")
    @Operation(summary = "Get conversations by date range", description = "Retrieve conversations within a date range")
    public ResponseEntity<List<ConversationResource>> getConversationsByDateRange(
            @RequestParam @Parameter(description = "Start date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @Parameter(description = "End date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(required = false) @Parameter(description = "Bot ID filter") UUID botId) {
        try {
            List<Conversation> conversations;
            if (botId != null) {
                conversations = conversationRepository.findByChatbotIdAndDateRange(botId, startDate, endDate);
            } else {
                conversations = conversationRepository.findByDateRange(startDate, endDate);
            }
            return ResponseEntity.ok(conversationMapper.toResources(conversations));
        } catch (Exception e) {
            log.error("Error retrieving conversations by date range: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}