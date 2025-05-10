package be.fgov.bosa.chatbot.springaimcpragguardrails.mappers;


import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ChatbotResource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ChatBotMapper {
    ChatbotResource toResource(Chatbot model);

    List<ChatbotResource> toResources(List<Chatbot> models);

    Chatbot toModel(ChatbotResource resource);
}
