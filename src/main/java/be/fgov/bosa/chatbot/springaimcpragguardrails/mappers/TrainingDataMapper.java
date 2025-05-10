package be.fgov.bosa.chatbot.springaimcpragguardrails.mappers;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.TrainingData;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.TrainingDataResource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface TrainingDataMapper {
    TrainingDataResource toResource(TrainingData model);

    List<TrainingDataResource> toResources(List<TrainingData> models);

    TrainingData toModel(TrainingDataResource resource);

}
