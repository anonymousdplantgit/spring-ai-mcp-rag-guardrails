package be.fgov.bosa.chatbot.springaimcpragguardrails.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID; /**
 * Bot statistics data transfer object
 */
@Data
@Builder
@AllArgsConstructor
public class BotStatistics {
    private UUID botId;
    private Long totalConversations;
    private Long activeConversations;
    private Double avgMessagesPerConversation;
    private Long totalMessages;
    private Long userMessages;
    private Long botMessages;
    private Double avgProcessingTime;
    private Double avgConfidence;
    private Integer totalInputTokens;
    private Integer totalOutputTokens;
    private Map<String, Long> dailyActivity;
    private Map<String, Long> hourlyActivity;
    private Map<String, Long> popularTopics;
    private Double averageConversationDuration;
    private Double userRetentionRate;
    private Double messageResponseRate;
    private OffsetDateTime lastActivityTime;
}
