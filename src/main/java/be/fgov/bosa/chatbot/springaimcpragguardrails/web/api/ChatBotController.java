package be.fgov.bosa.chatbot.springaimcpragguardrails.web.api;

import be.fgov.bosa.chatbot.springaimcpragguardrails.services.ChatBotService;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.ChatRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ChatResponse;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ChatbotResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value="/api/chatbots", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ChatBots", description = "Manage Chatbots in the system")
public class ChatBotController {

    private final ChatBotService chatBotService;

    @PostMapping("/rag")
    public ChatResponse ragChat(@RequestBody ChatRequest request) {
        return chatBotService.queryLLM(request);
    }

    @PostMapping(value = "/upload-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadDocument(
            @RequestParam String botId,
            @RequestPart("file") MultipartFile file) {
        try {
            chatBotService.ingestPDF(file,botId );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();

        }
    }

    @DeleteMapping(value = "/{botId}")
    public ResponseEntity<Void> clearBotKnowledgeBase(
            @PathVariable String botId) {
        try {
            chatBotService.clearEmbeddingsByBotId(botId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);        }
    }

    @PostMapping()
    public ResponseEntity<ChatbotResource> saveChatbot(@RequestBody ChatbotResource request) {
        return  ResponseEntity.ok(chatBotService.save(request));
    }
    @GetMapping("/paged")
    public ResponseEntity<Page<ChatbotResource>> findChatbots(@ModelAttribute LazyLoadingEventRequest request) {
        return  ResponseEntity.ok(chatBotService.findItems(request));
    }
    @GetMapping
    public ResponseEntity<List<ChatbotResource>> getAllChatbots() {
        return ResponseEntity.ok(chatBotService.getAllBots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatbotResource> getChatbot(@PathVariable UUID id) {
        return ResponseEntity.ok(chatBotService.getChatbot(id));
    }
}
