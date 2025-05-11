package be.fgov.bosa.chatbot.springaimcpragguardrails.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Conversation summary DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationSummary {
    private UUID conversationId;
    private Integer totalMessages;
    private Long userMessages;
    private Long botMessages;
    private Integer totalTokens;
    private OffsetDateTime startTime;
    private OffsetDateTime lastActivity;
    private String duration;
}
