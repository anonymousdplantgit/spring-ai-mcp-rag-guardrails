package be.fgov.bosa.chatbot.springaimcpragguardrails.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID; /**
 * Conversation summary report
 */
@Data
@Builder
@AllArgsConstructor
public class ConversationSummaryReport {
    private UUID botId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private Long totalConversations;
    private Long completedConversations;
    private Long activeConversations;
    private Long totalMessages;
    private Double avgMessagesPerConversation;
    private Integer peakActiveHour;
    private Map<Integer, Long> hourlyDistribution;
}
