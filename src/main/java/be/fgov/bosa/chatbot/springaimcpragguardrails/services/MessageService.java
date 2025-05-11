package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Message;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ChatbotRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ConversationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.MessageRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dtos.ConversationSummary;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationReviewStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.MessageTypeEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.mappers.MessageMapper;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.MessageResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ChatbotRepository chatbotRepository;
    private final MessageMapper messageMapper;

    /**
     * Save a user message with explicit bot ID
     */
    @Transactional
    public MessageResource saveUserMessage(UUID conversationId, String content, UUID botId) {
        Conversation conversation = getOrCreateConversation(conversationId, botId);

        Message message = Message.builder()
                .content(content)
                .type(MessageTypeEnum.USER)
                .timestamp(OffsetDateTime.now())
                .processingTimeMs(0L)
                .build();

        conversation.addMessage(message);
        conversationRepository.save(conversation);

        Message savedMessage = messageRepository.save(message);
        log.debug("Saved user message for conversation {}: {}", conversationId, savedMessage.getId());

        return messageMapper.toResource(savedMessage);
    }

    /**
     * Save a bot response message with metadata
     */
    @Transactional
    public MessageResource saveBotMessage(
            UUID conversationId,
            String content,
            Integer inputTokens,
            Integer outputTokens,
            Double confidence,
            Long processingTimeMs,
            UUID botId) {

        Conversation conversation = getOrCreateConversation(conversationId, botId);

        Message message = Message.builder()
                .content(content)
                .type(MessageTypeEnum.BOT)
                .timestamp(OffsetDateTime.now())
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .confidence(confidence)
                .processingTimeMs(processingTimeMs)
                .build();

        conversation.addMessage(message);
        conversationRepository.save(conversation);

        Message savedMessage = messageRepository.save(message);
        log.debug("Saved bot message for conversation {}: {}", conversationId, savedMessage.getId());

        return messageMapper.toResource(savedMessage);
    }

    /**
     * Get all messages for a conversation
     */
    @Transactional(readOnly = true)
    public List<MessageResource> getConversationMessages(UUID conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        return messageMapper.toResources(messages);
    }

    /**
     * Get messages by external conversation ID
     */
    @Transactional(readOnly = true)
    public List<MessageResource> getConversationMessagesByExternalId(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

        return messageMapper.toResources(conversation.getMessages());
    }

    /**
     * Get messages with pagination
     */
    @Transactional(readOnly = true)
    public Page<MessageResource> getConversationMessagesPage(UUID conversationId, Pageable pageable) {
        Page<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId, pageable);
        return messages.map(messageMapper::toResource);
    }

    /**
     * Delete messages for a conversation
     */
    @Transactional
    public void deleteConversationMessages(UUID conversationId) {
        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        messageRepository.deleteAll(messages);
        log.info("Deleted {} messages for conversation {}", messages.size(), conversationId);
    }

    /**
     * Get message count for a bot
     */
    @Transactional(readOnly = true)
    public long getMessageCountForBot(UUID botId) {
        return messageRepository.countMessagesByBotId(botId);
    }

    /**
     * Get recent messages for a bot
     */
    @Transactional(readOnly = true)
    public List<MessageResource> getRecentMessagesForBot(UUID botId, int limit) {
        List<Message> messages = messageRepository.findRecentMessagesByBotId(botId, PageRequest.of(0, limit));
        return messageMapper.toResources(messages);
    }

    /**
     * Get conversation history summary
     */
    @Transactional(readOnly = true)
    public ConversationSummary getConversationSummary(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));

        long userMessages = conversation.getMessages().stream()
                .filter(m -> m.getType() == MessageTypeEnum.USER)
                .count();

        long botMessages = conversation.getMessages().stream()
                .filter(m -> m.getType() == MessageTypeEnum.BOT)
                .count();

        int totalTokens = conversation.getMessages().stream()
                .filter(m -> m.getInputTokens() != null && m.getOutputTokens() != null)
                .mapToInt(m -> m.getInputTokens() + m.getOutputTokens())
                .sum();

        return ConversationSummary.builder()
                .conversationId(conversationId)
                .totalMessages(conversation.getTotalMessages())
                .userMessages(userMessages)
                .botMessages(botMessages)
                .totalTokens(totalTokens)
                .startTime(conversation.getStartTime())
                .lastActivity(conversation.getLastActivity())
                .duration(getDuration(conversation))
                .build();
    }

    /**
     * Get or create conversation with explicit bot ID
     */
    private Conversation getOrCreateConversation(UUID conversationId, UUID botId) {
        if(conversationId != null) {
            return conversationRepository.findById(conversationId).orElseThrow(() -> new IllegalArgumentException("Conversation not found: " + conversationId));
        }
        Chatbot chatbot = chatbotRepository.findById(botId)
                .orElseThrow(() -> new IllegalArgumentException("Chatbot not found: " + botId));

        Conversation newConversation = Conversation.builder()
                .startTime(OffsetDateTime.now())
                .chatbot(chatbot)
                .reviewStatus(ConversationReviewStatusEnum.PENDING)
                .status(ConversationStatusEnum.STARTED)
                .totalMessages(0)
                .lastActivity(OffsetDateTime.now())
                .build();
        return conversationRepository.save(newConversation);
    }

    /**
     * Calculate conversation duration
     */
    private String getDuration(Conversation conversation) {
        if (conversation.getEndTime() != null) {
            Duration duration = Duration.between(conversation.getStartTime(), conversation.getEndTime());
            return formatDuration(duration);
        } else {
            Duration duration = Duration.between(conversation.getStartTime(), OffsetDateTime.now());
            return formatDuration(duration) + " (ongoing)";
        }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    /**
     * Search messages across conversations
     */
    @Transactional(readOnly = true)
    public Page<MessageResource> searchMessages(String searchText, Pageable pageable) {
        Page<Message> messages = messageRepository.searchMessagesGlobal(searchText, pageable);
        return messages.map(messageMapper::toResource);
    }

    /**
     * Search messages within a specific conversation
     */
    @Transactional(readOnly = true)
    public List<MessageResource> searchMessagesInConversation(UUID conversationId, String searchText) {
        List<Message> messages = messageRepository.searchMessagesInConversation(conversationId, searchText);
        return messageMapper.toResources(messages);
    }
}
