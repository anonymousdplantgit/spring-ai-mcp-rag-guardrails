package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.LanguageEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.PriorityEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.TrainingDataTypeEnum;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class TrainingDataResource {
    private UUID id;
    
    private String title;
    
    private TrainingDataTypeEnum type;

    private LanguageEnum language;
    
    private PriorityEnum priority;
    
    private OffsetDateTime contentDate;
    
    private OffsetDateTime addedDate = OffsetDateTime.now();
    
    private ChatbotResource chatbot;
    
}

