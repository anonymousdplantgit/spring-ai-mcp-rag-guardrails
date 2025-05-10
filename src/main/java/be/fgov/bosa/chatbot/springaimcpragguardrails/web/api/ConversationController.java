package be.fgov.bosa.chatbot.springaimcpragguardrails.web.api;

import be.fgov.bosa.chatbot.springaimcpragguardrails.services.ConversationService;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ConversationResource;
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
@RequestMapping(value = "/api/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Manage conversations in the system")
public class ConversationController {

    private final ConversationService service;

    @GetMapping(value = "/search")
    public ResponseEntity<List<ConversationResource>> searchConversations(
            @RequestParam @Parameter(description = "Search query for conversation autocomplete") String query) {
        return ResponseEntity.ok(service.autocomplete(query));
    }

    @GetMapping
    public ResponseEntity<Page<ConversationResource>> getConversations(
            @ModelAttribute @Parameter(description = "Lazy loading request parameters") LazyLoadingEventRequest request) {
        return ResponseEntity.ok(service.findItems(request));
    }

    @PostMapping
    public ResponseEntity<ConversationResource> saveConversation(
            @RequestBody @Parameter(description = "Conversation details") ConversationResource item) {
        return ResponseEntity.ok(service.saveConversation(item));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResource> getConversation(
            @PathVariable @Parameter(description = "UUID of the conversation") UUID id) {
        return ResponseEntity.ok(service.findConversation(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable @Parameter(description = "UUID of the conversation") UUID id) {
        service.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }
}
