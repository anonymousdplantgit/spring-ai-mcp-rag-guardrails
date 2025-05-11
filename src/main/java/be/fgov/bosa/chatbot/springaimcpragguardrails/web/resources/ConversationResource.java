package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationReviewStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ConversationStatusEnum;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ConversationResource {
    private UUID id;
    private ChatbotResource chatbot;
    private ConversationStatusEnum status;
    private ConversationReviewStatusEnum reviewStatus;
    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private OffsetDateTime lastActivity = OffsetDateTime.now();
    private Integer totalMessages;
}