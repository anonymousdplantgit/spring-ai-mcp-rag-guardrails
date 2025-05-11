package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.MessageTypeEnum;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class MessageResource {
    private UUID id;
    private ConversationResource conversation;
    private String content;
    private OffsetDateTime timestamp;
    private MessageTypeEnum type;
    private Double confidence;
    private Long processingTimeMs;
    private Integer inputTokens;
    private Integer outputTokens;
}

