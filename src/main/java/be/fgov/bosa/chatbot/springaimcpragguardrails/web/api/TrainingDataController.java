package be.fgov.bosa.chatbot.springaimcpragguardrails.web.api;

import be.fgov.bosa.chatbot.springaimcpragguardrails.services.TrainingDataService;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.TrainingDataResource;
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
@RequestMapping(value = "/api/training-data", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Training Data", description = "Manage training data in the system")
public class TrainingDataController {

    private final TrainingDataService service;

    @GetMapping(value = "/search")
    public ResponseEntity<List<TrainingDataResource>> searchTrainingData(
            @RequestParam @Parameter(description = "Search query for training data autocomplete") String query) {
        return ResponseEntity.ok(service.autocomplete(query));
    }

    @GetMapping
    public ResponseEntity<Page<TrainingDataResource>> getTrainingData(
            @ModelAttribute @Parameter(description = "Lazy loading request parameters") LazyLoadingEventRequest request) {
        return ResponseEntity.ok(service.findItems(request));
    }

    @PostMapping
    public ResponseEntity<TrainingDataResource> saveTrainingData(
            @RequestBody @Parameter(description = "Training data details") TrainingDataResource item) {
        return ResponseEntity.ok(service.saveTrainingData(item));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingDataResource> getTrainingData(
            @PathVariable @Parameter(description = "UUID of the training data") UUID id) {
        return ResponseEntity.ok(service.findTrainingData(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrainingData(
            @PathVariable @Parameter(description = "UUID of the training data") UUID id) {
        service.deleteTrainingData(id);
        return ResponseEntity.noContent().build();
    }
}
