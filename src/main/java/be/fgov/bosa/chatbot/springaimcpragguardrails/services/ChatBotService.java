package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import be.fgov.bosa.chatbot.springaimcpragguardrails.advisors.LinksAppendingAdvisor;
import be.fgov.bosa.chatbot.springaimcpragguardrails.advisors.QueryLinksAugmenter;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.models.Chatbot;
import be.fgov.bosa.chatbot.springaimcpragguardrails.dao.repositories.ChatbotRepository;
import be.fgov.bosa.chatbot.springaimcpragguardrails.enums.ChatbotStatusEnum;
import be.fgov.bosa.chatbot.springaimcpragguardrails.mappers.ChatBotMapper;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.ChatRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.requests.LazyLoadingEventRequest;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ChatResponse;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.ChatbotResource;
import be.fgov.bosa.chatbot.springaimcpragguardrails.web.resources.MessageResource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;


@AllArgsConstructor
@Getter
@Setter
@Service
@Slf4j
public class ChatBotService {

    private final ChatbotRepository repository;
    private final ChatBotMapper mapper;
    private final ChatClient ragChatClient;
    private final RagService ragService;
    private final MessageService messageService;
    private final ChatMemory ragCallingChatMemory;
    private final VectorStore vectorStore;
    public ChatResponse queryLLM(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        // Validate bot exists and is active
        UUID botUuid = request.getBotId();
        Chatbot chatbot = repository.findById(botUuid)
                .orElseThrow(() -> new IllegalArgumentException("Chatbot not found with id: " + request.getBotId()));

        if (chatbot.getStatus() == ChatbotStatusEnum.ARCHIVED) {
            throw new IllegalStateException("Cannot chat with archived bot: " + chatbot.getName());
        }

        // Save user message with bot context
        MessageResource userMessage = messageService.saveUserMessage(
                request.getConversationId(),
                request.getMessage(),
                botUuid
        );

        UUID conversationId = userMessage.getConversation().getId();
        // Create a filter expression to match botId from the request
        Filter.Expression botIdFilter = new FilterExpressionBuilder().eq("botId", request.getBotId().toString()).build();
        DocumentRetriever documentRetriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.7)
                .filterExpression(botIdFilter) // Add the filter expression here
                .build();
        QueryAugmenter queryAugmenter = new QueryLinksAugmenter();
        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .order(1)
                .build();
        LinksAppendingAdvisor linksAppendingAdvisor
                = LinksAppendingAdvisor.builder().order(2).build();

        // Start building the prompt
        var promptBuilder = ragChatClient.prompt()
                .advisors(
                        retrievalAugmentationAdvisor,
                        linksAppendingAdvisor,
                        new MessageChatMemoryAdvisor(ragCallingChatMemory, conversationId.toString(), 10)
                )
                .options(ChatOptions.builder()
                        .temperature(chatbot.getTemperature())
                        .topK(5)
                        .build());
        // Conditionally add system prompt if not null
        if (chatbot.getSystemPromptTemplate() != null && !chatbot.getSystemPromptTemplate().isEmpty()) {
            promptBuilder = promptBuilder.system(chatbot.getSystemPromptTemplate());
        }
        // Complete the builder and get the response
        String response = promptBuilder
                .user(request.getMessage())
                .call()
                .content();
        // Calculate processing time
        long processingTime = System.currentTimeMillis() - startTime;
        // Save bot response message with all metadata and bot context
        MessageResource botMessage = messageService.saveBotMessage(
                request.getConversationId(),
                response,
                0,
                0,
                0.0,
                processingTime,
                botUuid
        );
        return ChatResponse.builder().response(response).conversationId(conversationId).build();
    }

    public ChatbotResource save(ChatbotResource request) {
        return this.mapper.toResource(this.repository.save(this.mapper.toModel(request)));
    }

    public ChatbotResource getChatbot(UUID id) {
        return this.mapper.toResource(this.repository.getReferenceById(id));
    }
    public List<ChatbotResource> getAllBots() {
        return mapper.toResources(repository.findAll());
    }

    public Page<ChatbotResource> findItems(LazyLoadingEventRequest request) {
        Sort.Direction direction = null;
        if (request.getSortField() != null) {
            direction = request.getSortOrder().equals(1) ? Sort.Direction.ASC : Sort.Direction.DESC;
        }
        Pageable pageRequest = PageRequest.of(
                request.getFirst() / request.getRows(),
                request.getRows(),
                direction != null ? Sort.by(direction, request.getSortField()) : Sort.by(Sort.Direction.ASC, "creationDate"));
        return repository.findItems(request.getGlobalFilter(), pageRequest).map(mapper::toResource);

    }

    public void ingestPDF(MultipartFile file, String botId) throws IOException {
        this.ragService.ingestPDF(file,botId);
    }

    public void clearEmbeddingsByBotId(String botId) {
        this.ragService.clearEmbeddingsByBotId(botId);
    }
}