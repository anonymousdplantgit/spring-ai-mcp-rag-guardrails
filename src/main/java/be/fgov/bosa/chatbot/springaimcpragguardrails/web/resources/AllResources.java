package be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Resource;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AllResources {
    ChatbotResource chatbot;
    TrainingDataResource trainingData;
    Resource resource;
    ConversationResource conversation;
    MessageResource message;
    OrganizationResource organization;

}
