package be.fgov.bosa.chatbot.springaimcpragguardrails.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime; /**
 * User engagement metrics
 */
@Data
@Builder
@AllArgsConstructor
public class UserEngagementMetrics {
    private Double avgConversationDuration;
    private Double retentionRate;
    private Double responseRate;
    private OffsetDateTime lastActivityTime;
}
