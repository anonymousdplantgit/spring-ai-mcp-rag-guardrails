package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Organization;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.OrganizationRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.mappers.OrganizationMapper;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.OrganizationResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationService {
    private static final Sort SORTING_DESC = Sort.by(Sort.Direction.DESC, "lastModifiedDate");
    private final OrganizationRepository repository;
    private final OrganizationMapper mapper;

    public List<OrganizationResource> autocomplete(String value) {
        return mapper.toResources(repository.autocomplete( value, PageRequest.of(0, 10)).getContent());
    }

    public OrganizationResource saveOrganisation(@RequestBody OrganizationResource resource) {
        Organization model = mapper.toModel(resource);
        return mapper.toResource(repository.save(model));
    }

    public OrganizationResource findOrganisation(UUID OrganisationId) {
        return mapper.toResource(repository.getReferenceById(OrganisationId));
    }

    public boolean deleteOrganisation(UUID destinationId) {
        repository.deleteById(destinationId);
        return true;
    }

    public Page<OrganizationResource> findItems(LazyLoadingEventRequest request) {
        Sort.Direction direction = null;
        if (request.getSortField() != null) {
            direction = request.getSortOrder().equals(1) ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        Pageable pageRequest = PageRequest.of(
                request.getFirst() / request.getRows(),
                request.getRows(),
                direction != null ? Sort.by(direction, request.getSortField()) : SORTING_DESC);
        return repository.findItems(request.getGlobalFilter(), pageRequest).map(mapper::toResource);
    }
}