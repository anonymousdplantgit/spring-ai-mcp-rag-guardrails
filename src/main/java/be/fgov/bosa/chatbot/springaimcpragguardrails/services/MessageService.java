package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Message;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ChatbotRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ConversationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.MessageRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationReviewStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.MessageTypeEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.mappers.MessageMapper;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.MessageResource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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
}
