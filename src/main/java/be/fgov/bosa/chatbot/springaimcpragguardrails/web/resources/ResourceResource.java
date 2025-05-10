package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.PriorityEnum;
import lombok.*;

import java.util.UUID;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ResourceResource {
    private UUID id;
    
    private String name;
    
    private String url;
    
    private PriorityEnum priority;
    
    private TrainingDataResource trainingData;
}

