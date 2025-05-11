package be.fgov.bosa.chatbot.springaimcpragguardrails.dtos;

/**
 * Interface for conversation statistics projection
 */
public interface ConversationStatistics {
    Long getTotalConversations();
    Double getAvgMessagesPerConversation();
    Long getActiveConversations();
}
