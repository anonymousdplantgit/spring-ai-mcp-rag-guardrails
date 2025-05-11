package be.fgov.bosa.chatbot.springaimcpragguardrails.mappers;


import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Message;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.MessageResource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {ConversationMapper.class})
public interface MessageMapper {

    MessageResource toResource(Message model);

    List<MessageResource> toResources(List<Message> models);

    Message toModel(MessageResource resource);
}
