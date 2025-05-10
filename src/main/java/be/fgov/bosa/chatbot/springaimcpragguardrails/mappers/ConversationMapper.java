package be.fgov.bosa.chatbot.springaimcpragguardrails.mappers;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Conversation;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ConversationResource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConversationMapper {
    ConversationResource toResource(Conversation model);

    List<ConversationResource> toResources(List<Conversation> models);

    Conversation toModel(ConversationResource resource);
}
