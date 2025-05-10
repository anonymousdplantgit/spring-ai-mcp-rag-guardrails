package be.fgov.bosa.chatbot.springaimcpragguardrails.mappers;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Organization;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.OrganizationResource;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface OrganizationMapper {
    OrganizationResource toResource(Organization model);

    List<OrganizationResource> toResources(List<Organization> models);

    Organization toModel(OrganizationResource resource);

}
