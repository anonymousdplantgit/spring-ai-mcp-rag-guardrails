package be.fgov.bosa.chatbot.springaimcpragguardrails.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class RagService {

    private final VectorStore vectorStore;



    public void ingestPDF(MultipartFile file, String botId) throws IOException {

        // 2. Create upload directory if needed
        File directory = new File("temp");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 3. Generate unique filename and save
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get("temp", fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Resource pdfResource = new UrlResource(filePath.toUri());
        // Spring AI utility class to read a PDF file page by page
        // Extract
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(3) // Specifies that the bottom 3 lines of text on each page should be deleted.
                                .withNumberOfTopPagesToSkipBeforeDelete(1) // Indicates that the text deletion rule should not apply to the first page.
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        // Transform
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();

        log.info("Parsing document, splitting, creating embeddings, and storing in vector store...");

        // tag as external knowledge in the vector store's metadata
        List<Document> splitDocuments = tokenTextSplitter.split(pdfReader.read());
        for (Document splitDocument: splitDocuments) { // footnotes
            splitDocument.getMetadata().put("filename", pdfResource.getFilename());
            splitDocument.getMetadata().put("version", 1);
            splitDocument.getMetadata().put("botId", botId);
        }

        // Sending batch of documents to vector store
        // Load
        vectorStore.write(splitDocuments);

        log.info("Done parsing document, splitting, creating embeddings and storing in vector store.");
    }

    public void clearEmbeddingsByBotId(String botId) {
        log.info("Clearing embeddings for botId: {}", botId);
        Filter.Expression filterExpression = new Filter.Expression(
                Filter.ExpressionType.EQ,
                new Filter.Key("botId"),
                new Filter.Value(botId)
        );
        // Filter and delete documents with the specified botId
        vectorStore.delete(filterExpression);

        log.info("Embeddings cleared for botId: {}", botId);
    }
}
