package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

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
    
    private String conversationId;
    
    private ChatbotResource chatbot;

    private ConversationStatusEnum status;

    private OffsetDateTime startTime;

}