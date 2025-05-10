package be.fgov.bosa.chatbot.springaimcpragguardrails.web.api;

import be.fgov.bosa.chatbot.springaimcpragguardrails.services.OrganizationService;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.OrganizationResource;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/organisations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Organisations", description = "Manage organisations in the system")
public class OrganisationController {
    
    private final OrganizationService service;


    @GetMapping(value = "/search")
    public ResponseEntity<List<OrganizationResource>> searchOrganisations(
            @RequestParam @Parameter(description = "Search query for organisation autocomplete") String query) {
        return ResponseEntity.ok(service.autocomplete(query));
    }


    public ResponseEntity<Page<OrganizationResource>> getOrganisations(
            @ModelAttribute @Parameter(description = "Lazy loading request parameters") LazyLoadingEventRequest request) {
        return ResponseEntity.ok(service.findItems(request));
    }

    @PostMapping
    public ResponseEntity<OrganizationResource> saveOrganisation(
            @RequestBody @Parameter(description = "Organisation details") OrganizationResource item) {
        return ResponseEntity.ok(service.saveOrganisation(item));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResource> getOrganisation(
            @PathVariable @Parameter(description = "UUID of the organisation") UUID id) {
        return ResponseEntity.ok(service.findOrganisation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganisation(
            @PathVariable @Parameter(description = "UUID of the organisation") UUID id) {
        service.deleteOrganisation(id);
        return ResponseEntity.noContent().build();
    }

}
